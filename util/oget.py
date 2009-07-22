#!/usr/bin/python
"""
Pull a oAuth protected page from foursquare.

Expects ~/.oget to contain (one on each line):
CONSUMER_KEY
CONSUMER_KEY_SECRET
USERNAME
PASSWORD

Don't forget to chmod 600 the file!
"""

import httplib
import os
import re
import sys
import urllib
import urllib2
import urlparse
import user
from xml.dom import pulldom
from xml.dom import minidom

import oauth

"""From: http://groups.google.com/group/foursquare-api/web/oauth

@consumer = OAuth::Consumer.new("consumer_token","consumer_secret", {
       :site               => "http://playfoursquare.com",
       :scheme             => :header,
       :http_method        => :post,
       :request_token_path => "/oauth/request_token",
       :access_token_path  => "/oauth/access_token",
       :authorize_path     => "/oauth/authorize"
      })
"""

SERVER = 'api.playfoursquare.com'
PORT = 80

CONTENT_TYPE_HEADER = {'Content-Type' :'application/x-www-form-urlencoded'}

SIGNATURE_METHOD = oauth.OAuthSignatureMethod_PLAINTEXT()

AUTHEXCHANGE_URL = 'http://api.playfoursquare.com/v1/authexchange'

CONSUMER_KEY = 'd418b4030b320f6b5ba2810a25075b5204a1bffb5'
CONSUMER_SECRET = 'fecb671c5edb036e9abb963b67e05d18'

FS_USERNAME = 'testuser@joelapenna.com'
FS_PASSWORD = 'ci9ahXa9'

CITIES_URL = 'http://api.playfoursquare.com/v1/cities'


def parse_auth_response(auth_response):
  return (
      re.search('<oauth_token>(.*)</oauth_token>', auth_response).groups()[0],
      re.search('<oauth_token_secret>(.*)</oauth_token_secret>',
        auth_response).groups()[0]
  )


def create_signed_oauth_request(consumer):
  oauth_request = oauth.OAuthRequest.from_consumer_and_token(
      consumer, http_method='POST', http_url=AUTHEXCHANGE_URL,
      parameters=dict(fs_username=FS_USERNAME, fs_password=FS_PASSWORD))

  oauth_request.sign_request(SIGNATURE_METHOD, consumer, None)
  return oauth_request


def main():
  url = urlparse.urlparse(sys.argv[1])
  # Nevermind that the query can have repeated keys.
  parameters = dict(urlparse.parse_qsl(url.query))

  passwd_file = open(os.path.join(user.home, '.oget'))
  lines = [line.strip() for line in passwd_file.readlines()]

  if len(lines) == 4:
    cons_key, cons_key_secret, username, passwd = lines
    access_token = None
  else:
    cons_key, cons_key_secret, username, passwd, token, secret = lines
    access_token = oauth.OAuthToken(token, secret)

  consumer = oauth.OAuthConsumer(cons_key, cons_key_secret)

  if not access_token:
    oauth_request = create_signed_oauth_request(consumer)

    connection = httplib.HTTPConnection('api.playfoursquare.com:80')

    connection.request(oauth_request.http_method, oauth_request.to_url(),
        headers={})

    auth_response = connection.getresponse().read()
    token = parse_auth_response(auth_response)
    access_token = oauth.OAuthToken(*token)
    open(os.path.join(user.home, '.oget'), 'w').write('\n'.join((
      cons_key, cons_key_secret, username, passwd, token[0], token[1])))

  oauth_request = oauth.OAuthRequest.from_consumer_and_token(consumer,
      access_token, http_method='POST', http_url=url.geturl(),
      parameters=parameters)
  oauth_request.sign_request(SIGNATURE_METHOD, consumer, access_token)

  connection = httplib.HTTPConnection('api.playfoursquare.com:80')
  connection.request(oauth_request.http_method, oauth_request.to_url(),
      body=oauth_request.to_postdata(), headers=CONTENT_TYPE_HEADER)

  #print connection.getresponse().read()
  print minidom.parse(connection.getresponse()).toprettyxml(indent='  ')


if __name__ == '__main__':
  main()

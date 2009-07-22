#!/usr/bin/python

import httplib
import re
import sys
import urllib
import urllib2
import urlparse
from xml.dom import pulldom

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

CONSUMER_KEY = None
CONSUMER_SECRET = None

FS_USERNAME = None
FS_PASSWORD = None


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

  consumer = oauth.OAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET)
  oauth_request = create_signed_oauth_request(consumer)

  connection = httplib.HTTPConnection('api.playfoursquare.com:80')

  connection.request(oauth_request.http_method, oauth_request.to_url(),
      headers={})

  auth_response = connection.getresponse().read()
  token = parse_auth_response(auth_response)
  access_token = oauth.OAuthToken(*token)

  oauth_request = oauth.OAuthRequest.from_consumer_and_token(consumer,
      access_token, http_method='POST', http_url=url.geturl(),
      parameters=parameters)
  oauth_request.sign_request(SIGNATURE_METHOD, consumer, access_token)

  connection = httplib.HTTPConnection('api.playfoursquare.com:80')
  connection.request(oauth_request.http_method, oauth_request.to_url(),
      body=oauth_request.to_postdata(), headers=CONTENT_TYPE_HEADER)
  response_body = connection.getresponse().read()

  #print connection.getresponse().read()
  print minidom.parse(connection.getresponse()).toprettyxml(indent='  ')


if __name__ == '__main__':
  main()

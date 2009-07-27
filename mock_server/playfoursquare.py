#!/usr/bin/python2.6
#
# Simple http server to emulate api.playfoursquare.com

import logging
import shutil
import urlparse

import SimpleHTTPServer
import BaseHTTPServer


class RequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):
  """Handle playfoursquare.com requests, for testing."""

  def do_GET(self):
    """Available attributes:
    client_address
    command, path
    headers (mimetools.Message)
    rfile
    wfile
    """
    logging.warn('do_GET: %s, %s', self.command, self.path)

    url = urlparse.urlparse(self.path)
    logging.warn('do_GET: %s', url)
    query = urlparse.parse_qs(url.query)
    query_keys = [pair[0] for pair in query]

    response = self.handle_url(url)
    if response != None:
      self.send_200()
      shutil.copyfileobj(response, self.wfile)
    self.wfile.close()

  do_POST = do_GET

  def handle_url(self, url):
    if url.path == '/v1/venue':
      return open('../captures/api/v1/venue.xml')
    elif url.path == '/v1/venues':
      return open('../captures/api/v1/venues.xml')
    elif url.path == '/v1/user':
      return open('../captures/api/v1/user.xml')
    elif url.path == '/v1/checkcity':
      return open('../captures/api/v1/checkcity.xml')
    elif url.path == '/v1/checkins':
      return open('../captures/api/v1/checkins.xml')
    elif url.path == '/v1/cities':
      return open('../captures/api/v1/cities.xml')
    elif url.path == '/v1/switchcity':
      return open('../captures/api/v1/switchcity.xml')
    elif url.path == '/v1/tips':
      return open('../captures/api/v1/tips.xml')
    else:
      self.send_error(404)

  def send_200(self):
    self.send_response(200)
    self.send_header('Content-type', 'text/xml')
    self.end_headers()


def main():
  server_address = ('0.0.0.0', 8080)
  httpd = BaseHTTPServer.HTTPServer(server_address, RequestHandler)

  sa = httpd.socket.getsockname()
  print "Serving HTTP on", sa[0], "port", sa[1], "..."
  httpd.serve_forever()


if __name__ == '__main__':
  main()

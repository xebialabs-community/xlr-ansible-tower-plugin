#!flask/bin/python
#
# Copyright 2020 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

import logging
from logging.handlers import RotatingFileHandler
from flask import Flask, request, jsonify, make_response
from time import strftime
import traceback
from werkzeug.exceptions import HTTPException, BadRequest, NotFound, Unauthorized
from functools import wraps
import os, io, json

app = Flask(__name__)
handler = RotatingFileHandler('ansibleTowerPlugin.log', maxBytes=1000000, backupCount=1)
logger_formatter = logging.Formatter('%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s')
handler.setFormatter(logger_formatter)
handler.setLevel(logging.DEBUG)
app.logger.addHandler(handler)

def getFile( fileName, status="200" ):
     filePath = "/ansibletower-stub/responses/%s" % fileName
     if not os.path.isfile(filePath):
        raise NotFound({"code": "response_file_not_found", "description": "Unable to load response file"}, 500)

     f = io.open(filePath, "r", encoding="utf-8")

     resp = make_response( (f.read(), status) )
     resp.headers['Content-Type'] = 'application/json; charset=utf-8'

     return resp

def requires_auth(f):
    """
    Determines if the access token is valid
    """
    @wraps(f)
    def decorated(*args, **kwargs):
        token = get_token_auth_header()
        if token != "YWRtaW46YWRtaW4=": # admin:admin in base64
          raise Unauthorized({"code": "invalid_header", "description": "Unable to find appropriate key"}, 400)
        return f(*args, **kwargs)

    return decorated

@app.route('/')
def index():
    return "Hello, World!"

@app.route('/api/v2/inventory_updates/<id>/', methods=['GET'])
@requires_auth
def getInventoryUpdate(id):
    return getFile("inventory-sync-%s.json" % id)

@app.route('/api/v2/inventory_updates/<id>/stdout/', methods=['GET'])
@requires_auth
def getInventoryUpdateStdout(id):
    # note: currently the 'content' field on these json files is base64 encoded, as that is how the CLI requests it
    return getFile("inventory-sync-stdout-%s.json" % id)

@app.route('/api/v2/inventory_sources/<inventory_source_id>/', methods=['GET'])
@requires_auth
def getInventorySource(inventory_source_id):
    resp = make_response( ("{ \"inventory\": \"1337\"}", 200) )
    resp.headers['Content-Type'] = 'application/json; charset=utf-8'
    return resp

@app.route('/api/v2/inventory_sources/<inventory_source_id>/update/', methods=['GET', 'POST'])
@requires_auth
def startSync(inventory_source_id):
    if request.method == 'GET':
        resp = make_response( ("{ \"can_update\": \"true\"}", 200) )
        resp.headers['Content-Type'] = 'application/json; charset=utf-8'
        return resp

    app.logger.debug("startSync for inventory source %s" % inventory_source_id)

    resp = make_response((getFile("inventory-sync-update-%s.json" % inventory_source_id), 202))
    return resp

@app.route('/api/v2/job_templates/<id>/', methods=['GET'])
@requires_auth
def launchJob(id):
    app.logger.debug("In Launch Job, id = %s" % (id))
    return getFile("launchJob.json")

@app.route('/api/v2/job_templates/<id>/launch/', methods=['GET'])
@requires_auth
def getLaunch(id):
    app.logger.debug("In getLaunch, id = %s" % (id))
    return getFile("getLaunch.json")

@app.route('/api/v2/job_templates/<id>/launch/', methods=['POST'])
@requires_auth
def postLaunch(id):
    app.logger.debug("In postLaunch, id = %s" % (id))
    return getFile("postLaunch.json")

@app.route('/api/v2/jobs/<id>/')
@requires_auth
def getJobs(id):
    app.logger.debug("In getJobs, id = %s" % (id))
    return getFile("getJobs.json")


def get_token_auth_header():
    """
    Obtains the access token from the Authorization Header
    """
    auth = request.headers.get("Authorization", None)
    if not auth:
        raise AuthError({"code": "authorization_header_missing",
                        "description": "Authorization header is expected"}, 401)

    parts = auth.split()

    if parts[0] != "Basic":
        raise AuthError({"code": "invalid_header",
                        "description":
                            "Authorization header must start with Basic"}, 401)
    token = parts[1]
    return token

# Added for debug purposes - logging all requests
@app.route("/json")
def get_json():
    data = {"Name":"Some Name","Books":"[Book1, Book2, Book3]"}
    return jsonify(data_WRONG) # INTENTIONAL ERROR FOR TRACEBACK EVENT

@app.after_request
def after_request(response):
    timestamp = strftime('[%Y-%b-%d %H:%M]')
    app.logger.error('%s %s %s %s %s %s', timestamp, request.remote_addr, request.method, request.scheme, request.full_path, response.status)
    return response

@app.errorhandler(Exception)
def exceptions(e):
    tb = traceback.format_exc()
    timestamp = strftime('[%Y-%b-%d %H:%M]')
    app.logger.error('%s %s %s %s %s 5xx INTERNAL SERVER ERROR\n%s', timestamp, request.remote_addr, request.method, request.scheme, request.full_path, tb)
    return e


if __name__ == '__main__':
    app.run()
#
# Copyright 2018 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

from tower_cli import get_resource
from ansible_tower.connect_util import session

def process(task_vars):
    with session(task_vars['tower_server'], task_vars['username'], task_vars['password']):
        job = get_resource('job')
        inventory = None
        if task_vars['inventory']:
            inventory = task_vars['inventory']
        try:
            print("\n```")  # started markdown code block
            extraVars = task_vars['extraVars']
            if task_vars['inventory']:
                extraVars.append(u"inventory: %s" % task_vars['inventory'])
            if task_vars['credential']:
                extraVars.append(u"credential: %s" % task_vars['credential'])
            preparedExtraVars = map(lambda v: v.replace(taskPasswordToken, taskPassword),extraVars)
            res = job.launch(job_template=task_vars['jobTemplate'], monitor=task_vars['waitTillComplete'], extra_vars=preparedExtraVars)
        finally:
            print("```\n")  # end markdown code block

        globals()['jobId'] = res['id']
        globals()['jobStatus'] = res['status']
        print("[Job %s Link](%s/#jobs/%s)" % (res['id'], task_vars['tower_server']['url'], res['id']))
        if task_vars['stopOnFailure'] and not res['status'] == 'successful':
            raise Exception("Failed with status %s" % res['status'])

if __name__ == '__main__' or __name__ == '__builtin__':
    process(locals())

#
# Copyright 2019 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

from tower_cli import get_resource
from ansible_tower.connect_util import session

def get_resource_id(resource, name_or_id):
    if name_or_id.isdigit():
        return int(name_or_id)
    result = resource.list(name=name_or_id)
    count = int(result['count'])
    if count == 0:
        raise Exception("Resource name '%s''%s' not found " % (resource, name_or_id))
    if count > 1:
        raise Exception("Too many result for resource name '%s''%s' not found " % (resource, name_or_id))
    return int(result['results'][0]['id'])

def process(task_vars):
    with session(task_vars['tower_server'], task_vars['username'], task_vars['password']):
        job = get_resource('job')
        workflow_job = get_resource('workflow_job')     # adding the necessary call to start a workflow job as per https://tower-cli.readthedocs.io/en/latest/api_ref/resources/workflow_job.html

        try:
            k_vars = {}
            if task_vars['inventory']:
                result = get_resource_id(get_resource('inventory'), task_vars['inventory'])
                print("* set inventory : {0}->{1}".format(task_vars['inventory'], result))
                k_vars['inventory'] = result

            if task_vars['credential']:
                result = get_resource_id(get_resource('credential'), task_vars['credential'])
                print("* set credentials : {0}->{1}".format(task_vars['credential'], result))
                k_vars['credential'] = result

            if task_vars['extraVars2']:
                vars_ = str(task_vars['extraVars2'])
                print("* set extra_vars : {0}".format(vars_))
                # TODO: manage taskPasswordToken && taskPassword (turn hidden in waiting for...)
                k_vars['extra_vars'] = [vars_]

            print("\n")
            print("```")  # started markdown code block

            if task_vars['isTemplateWorkflow']:         # use the synthetic.xml new form checkbox to build a condition to differentiate a standard template job and a workflow template job
                                                        # not that using monitor here instead of wait will raise an exception.
                res = workflow_job.launch(workflow_job_template=task_vars['jobTemplate'],wait=task_vars['waitTillComplete'], **k_vars)
            else:
                res = job.launch(job_template=task_vars['jobTemplate'], monitor=task_vars['waitTillComplete'], **k_vars)

        finally:
            print("```")
            print("\n")  # end markdown code block

        globals()['jobId'] = res['id']
        globals()['jobStatus'] = res['status']

        if task_vars['isTemplateWorkflow']:             # use the synthetic.xml new form checkbox to build a condition to differentiate a standard template job and a workflow template job and provide the current job status URL
            print("* [Job %s Link](%s/#/workflow_jobs/%s)" % (res['id'], task_vars['tower_server']['url'], res['id']))
        else:
            print("* [Job %s Link](%s/#/jobs/%s)" % (res['id'], task_vars['tower_server']['url'], res['id']))

        if task_vars['stopOnFailure'] and not res['status'] == 'successful':
            raise Exception("Failed with status %s" % res['status'])

if __name__ == '__main__' or __name__ == '__builtin__':
    process(locals())

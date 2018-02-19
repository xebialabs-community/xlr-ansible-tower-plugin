
from tower_cli.conf import settings


def session(server):
    return settings.runtime_values(host=server['url'], verify_ssl=False, username=server['username'],
                                   password=server['password'])

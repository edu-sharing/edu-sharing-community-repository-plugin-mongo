## Parameters

### Global parameters

| Name                                      | Description                                    | Value                  |
| ----------------------------------------- | ---------------------------------------------- | ---------------------- |
| `global.annotations`                      | Define global annotations                      | `{}`                   |
| `global.cluster.domain`                   | Set global cluster domain                      | `cluster.local`        |
| `global.cluster.istio.enabled`            | Enable Istio Service mesh                      | `false`                |
| `global.cluster.pdb.enabled`              | Enable PDB                                     | `false`                |
| `global.cluster.storage.data.permission`  | Enable global custom data storage permissions  | `false`                |
| `global.cluster.storage.data.spec`        | Set data storage spec                          | `{}`                   |
| `global.cluster.storage.share.permission` | Enable global custom share storage permissions | `false`                |
| `global.cluster.storage.share.spec`       | Set share storage spec                         | `{}`                   |
| `global.debug`                            | Enable global debugging                        | `false`                |
| `global.image.pullPolicy`                 | Set global image pullPolicy                    | `Always`               |
| `global.image.pullSecrets`                | Set global image pullSecrets                   | `[]`                   |
| `global.image.registry`                   | Set global image container registry            | `${docker.registry}`   |
| `global.image.repository`                 | Set global image container repository          | `${docker.repository}` |
| `global.image.common`                     | Set global image container common              | `${docker.common}`     |
| `global.metrics.scrape.interval`          | Set prometheus scrape interval                 | `60s`                  |
| `global.metrics.scrape.timeout`           | Set prometheus scrape timeout                  | `60s`                  |
| `global.metrics.servicemonitor.enabled`   | Enable metrics service monitor                 | `false`                |
| `global.password`                         | Set global password                            | `""`                   |
| `global.security`                         | Set global security parameters                 | `{}`                   |

### Local parameters

| Name                                                       | Description                                         | Value                                                          |
| ---------------------------------------------------------- | --------------------------------------------------- | -------------------------------------------------------------- |
| `nameOverride`                                             | Override name                                       | `edusharing-repository-mongo`                                  |
| `image.name`                                               | Set image name                                      | `${docker.edu_sharing.community.common.mongodb.name}`          |
| `image.tag`                                                | Set image tag                                       | `${docker.edu_sharing.community.common.mongodb.tag}`           |
| `replicaCount`                                             | Define amount of parallel replicas to run           | `3`                                                            |
| `service.port.api`                                         | Set port for service API                            | `27017`                                                        |
| `service.port.metrics`                                     | Set port for metrics                                | `9216`                                                         |
| `config.database`                                          | Set database for mongo service                      | `repository`                                                   |
| `config.username`                                          | Set username for mongo service                      | `repository`                                                   |
| `config.password`                                          | Set password for mongo service                      | `""`                                                           |
| `config.replicaSet.key`                                    | Set replicaSet key for service                      | `repository`                                                   |
| `config.override`                                          | Set custom overrides                                | `{}`                                                           |
| `debug`                                                    | Enable debugging                                    | `false`                                                        |
| `nodeAffinity`                                             | Set node affinity                                   | `{}`                                                           |
| `podAntiAffinity`                                          | Set pod antiaffinity                                | `soft`                                                         |
| `tolerations`                                              | Set tolerations                                     | `[]`                                                           |
| `persistence.data.spec.accessModes`                        | Set access modes for persistent data                | `["ReadWriteOnce"]`                                            |
| `persistence.data.spec.resources.requests.storage`         | Set storage request for persistent data             | `5Gi`                                                          |
| `persistence.share.data.create`                            | Create persistent data share                        | `true`                                                         |
| `persistence.share.data.spec.accessModes`                  | Set access modes for persistent data share          | `["ReadWriteMany"]`                                            |
| `persistence.share.data.spec.resources.requests.storage`   | Set storage request for persistent data share       | `5Gi`                                                          |
| `podAnnotations`                                           | Set custom pod annotations                          | `{}`                                                           |
| `podSecurityContext.fsGroup`                               | Set fs group for access                             | `1000`                                                         |
| `podSecurityContext.fsGroupChangePolicy`                   | Set change policy for fs group                      | `OnRootMismatch`                                               |
| `securityContext.allowPrivilegeEscalation`                 | Allow privilege escalation                          | `false`                                                        |
| `securityContext.capabilities.drop`                        | Set drop capabilities                               | `["ALL"]`                                                      |
| `securityContext.runAsUser`                                | Define user to run under                            | `1000`                                                         |
| `terminationGracePeriod`                                   | Define grace period for termination                 | `120`                                                          |
| `startupProbe.failureThreshold`                            | Failure threshold for startupProbe                  | `30`                                                           |
| `startupProbe.initialDelaySeconds`                         | Initial delay seconds for startupProbe              | `0`                                                            |
| `startupProbe.periodSeconds`                               | Period seconds for startupProbe                     | `20`                                                           |
| `startupProbe.successThreshold`                            | Success threshold for startupProbe                  | `1`                                                            |
| `startupProbe.timeoutSeconds`                              | Timeout seconds for startupProbe                    | `10`                                                           |
| `livenessProbe.failureThreshold`                           | Failure threshold for livenessProbe                 | `3`                                                            |
| `livenessProbe.initialDelaySeconds`                        | Initial delay seconds for livenessProbe             | `30`                                                           |
| `livenessProbe.periodSeconds`                              | Period seconds for livenessProbe                    | `30`                                                           |
| `livenessProbe.timeoutSeconds`                             | Timeout seconds for livenessProbe                   | `10`                                                           |
| `readinessProbe.failureThreshold`                          | Failure threshold for readinessProbe                | `1`                                                            |
| `readinessProbe.initialDelaySeconds`                       | Initial delay seconds for readinessProbe            | `10`                                                           |
| `readinessProbe.periodSeconds`                             | Period seconds for readinessProbe                   | `10`                                                           |
| `readinessProbe.successThreshold`                          | Set threshold for success on readiness probe        | `1`                                                            |
| `readinessProbe.timeoutSeconds`                            | Timeout seconds for readinessProbe                  | `10`                                                           |
| `resources.limits.cpu`                                     | Set CPU limit on resources                          | `500m`                                                         |
| `resources.limits.memory`                                  | Set memory limit on resources                       | `2Gi`                                                          |
| `resources.requests.cpu`                                   | Set CPU for requests on resources                   | `500m`                                                         |
| `resources.requests.memory`                                | Set memory for requests on resources                | `2Gi`                                                          |
| `init.permission.image.name`                               | Set init container image name                       | `${docker.edu_sharing.community.common.minideb.name}`          |
| `init.permission.image.tag`                                | Set init container image tag                        | `${docker.edu_sharing.community.common.minideb.tag}`           |
| `init.permission.resources.limits.cpu`                     | Set init container CPU limit on resources           | `125m`                                                         |
| `init.permission.resources.limits.memory`                  | Set init container memory limit on resources        | `512Mi`                                                        |
| `init.permission.resources.requests.cpu`                   | Set init container CPU for requests on resources    | `125m`                                                         |
| `init.permission.resources.requests.memory`                | Set init container memory for requests on resources | `512Mi`                                                        |
| `init.permission.securityContext.runAsUser`                | Set user to run init container under                | `0`                                                            |
| `job.dump.image.name`                                      | Set dump job image name                             | `${docker.edu_sharing.community.common.mongodb.name}`          |
| `job.dump.image.tag`                                       | Set dump job image tag                              | `${docker.edu_sharing.community.common.mongodb.tag}`           |
| `job.dump.podAnnotations`                                  | Set dump job pod annotations                        | `{}`                                                           |
| `job.dump.resources.limits.cpu`                            | Set CPU limit on resources                          | `500m`                                                         |
| `job.dump.resources.limits.memory`                         | Set memory limit on resources                       | `2Gi`                                                          |
| `job.dump.resources.requests.cpu`                          | Set CPU for requests on resources                   | `500m`                                                         |
| `job.dump.resources.requests.memory`                       | Set memory for requests on resources                | `2Gi`                                                          |
| `job.dump.schedule`                                        | Set cron-like dump job schedule                     | `0 * * * *`                                                    |
| `job.dump.suspend`                                         | Suspend dump job                                    | `false`                                                        |
| `job.dump.securityContext.allowPrivilegeEscalation`        | Allow privilege escalation                          | `false`                                                        |
| `job.dump.securityContext.capabilities.drop`               | Set drop capabilities                               | `["ALL"]`                                                      |
| `job.dump.securityContext.runAsUser`                       | Define user to run under                            | `1000`                                                         |
| `sidecar.metrics.enabled`                                  | Enable metrics sidecar                              | `true`                                                         |
| `sidecar.metrics.image.name`                               | Set metrics sidecar image name                      | `${docker.edu_sharing.community.common.mongodb.exporter.name}` |
| `sidecar.metrics.image.tag`                                | Set metrics sidecar image tag                       | `${docker.edu_sharing.community.common.mongodb.exporter.tag}`  |
| `sidecar.metrics.relabelings`                              | Set metrics sidecar relabelings                     | `[]`                                                           |
| `sidecar.metrics.resources.limits.cpu`                     | Set CPU limit on resources                          | `125m`                                                         |
| `sidecar.metrics.resources.limits.memory`                  | Set memory limit on resources                       | `512Mi`                                                        |
| `sidecar.metrics.resources.requests.cpu`                   | Set CPU for requests on resources                   | `125m`                                                         |
| `sidecar.metrics.resources.requests.memory`                | Set memory for requests on resources                | `512Mi`                                                        |
| `sidecar.metrics.securityContext.allowPrivilegeEscalation` | Allow privilege escalation                          | `false`                                                        |
| `sidecar.metrics.securityContext.capabilities.drop`        | Set drop capabilities                               | `["ALL"]`                                                      |
| `sidecar.metrics.securityContext.runAsUser`                | Define user to run under                            | `1000`                                                         |

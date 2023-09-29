{{- define "edusharing_repository_mongo.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "edusharing_repository_mongo.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "edusharing_repository_mongo.labels" -}}
{{ include "edusharing_repository_mongo.labels.instance" . }}
helm.sh/chart: {{ include "edusharing_repository_mongo.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "edusharing_repository_mongo.labels.instance" -}}
{{ include "edusharing_repository_mongo.labels.app" . }}
{{ include "edusharing_repository_mongo.labels.version" . }}
{{- end -}}

{{- define "edusharing_repository_mongo.labels.version" -}}
version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end -}}

{{- define "edusharing_repository_mongo.labels.app" -}}
app: {{ include "edusharing_repository_mongo.name" . }}
app.kubernetes.io/name: {{ include "edusharing_repository_mongo.name" . }}
{{- end -}}

{{- define "edusharing_repository_mongo.image" -}}
{{- $registry := default .Values.global.image.registry .Values.image.registry -}}
{{- $repository := default .Values.global.image.repository .Values.image.repository -}}
{{ $registry }}{{ if $registry }}/{{ end }}{{ $repository }}{{ if $repository }}/{{ end }}
{{- end -}}

{{- define "edusharing_repository_mongo.pvc.share.data" -}}
share-data-{{ include "edusharing_repository_mongo.name" . }}
{{- end -}}

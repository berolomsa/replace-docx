# User guide for Replace DOCX Managing System

Application is created for managing *.docx templates with replace/download/delete/upload functions.
DOCX templates are being managed from UI interface http://url:8080/. Here you can delete/download/upload templates.

For using replace function client must send POST request to server.
Replaces docx file's placeholders
Replace keywords are dynamic
Find description below.
**POST URL - http://url:8080/replace**
	 
Request example:

    {
    "file":"template",
    "replace": {
    "name":"Bero",
    "surname":"Lomsadze",
    "idnumber":"1235",
    "date": "5 April",
    "phonenumber":"+995 12345678910",
    "address": "New York"
    }
    }

Params:
	"file" -> Filename of template without extention (For example: template will be referenced to template.docx
	"replace" -> Json array where key is placeholder and value is replacement. All keys will be upper-cased. (For example: name will 			actually replace $NAME$ in docx)
			Array keys are not fixed which makes replace function dynamic.

Response example:

    {
    "url": "http://localhost:8080/files/temps/a443f68f-8fbd-45ea-9673-53f478f75c4d.docx",
    "status": "OK"
    }

Params:
	"url" -> Generated temp file's URL from where client can download file
	"status" -> General status code

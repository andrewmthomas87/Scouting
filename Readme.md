# FRC Scouting App 2015 #
### JSON Messages ###
---
#### Log In ####
Client to Server
```javascript
{
    'type': "login"
    'scoutName': String
}
```
Server to Client
```javascript
{
    'CID': int
}
```
---
#### Prepare for next match  ####
Client to Server
```javascript
{
    'type': "prepare"
    'CID': int
}
```
Server to Client
```javascript
{
    'matchNumber': int,
    'teamNumber': int
}
```
---
#### Ready for start ####
Client to Server
```javascript
{
    'type': "ready"
    'CID': int,
    'matchNumber': int
}
```
Server to Client
```javascript
{
    'started': true
}
```
---
#### Contribution ####
Client to Server
```javascript
{
    'type': "contribution"
    'CID': int,
    'SID': int,
    'objects': String,  // '+' for addition to stack, '-' for subtraction from stack, and 'x' for knocked over removal
    'time': int,
    'autonomous': boolean
}
```
Server to Client
```javascript
{
    'updates': JSON
}
```
---
#### Waz up? ####
Client to Server
```javascript
{
    'type': "wazup"
    'CID': int
}
```
Server to Client
```javascript
{
    'updates': JSON
}
```
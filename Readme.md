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
#### Get next match ####
Supervisor to Server
'''javascript
{
    'type': "getNextMatch"
    'CID': int
}
'''
Server to Supervisor
'''javascript
{   // if error
    'type': 'status'
    'status': 'noNextMatch'
    'description': 'error text'
}
{   //else
    'type': 'nextMatchData'
    'matchNumber': int
    'redTeam1': int
    'redTeam2': int
    'redTeam3': int
    'blueTeam1': int
    'blueTeam2': int
    'blueTeam3': int
}
'''
---
#### Set match data ####
Supervisor to Server
'''javascript
{
    'type': 'setMatchData'
    'matchNumber': int
    'redTeam1': int
    'redTeam2': int
    'redTeam3': int
    'blueTeam1': int
    'blueTeam2': int
    'blueTeam3': int
}
'''
Server to Supervisor
'''javascript
{   // if fine
    'type': 'status'
    'status': 'ok'
}
{   // else
    'type': 'status'
    'status': 'badMessage'
}
'''
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
#### Get clients ####
Supervisor to Server
'''javascript
{
    'type': 'getClients'
}
'''
Server to Supervisor
'''javascript
{
    'type': 'connectedClients'
    'clients': [{'CID': int, 'scoutName': String, 'teamNumber': int}...] // if teamNumber == -1, teamNumber returned null
}
'''

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

#### Disconnect ####
Supervisor to Server
'''javascript
{
    'type': "disconnectClient"
    'disconnectCID': int
}
'''
Server to Supervisor
'''javascript
{
    'type': "status"
    'status': "ok"
}
'''
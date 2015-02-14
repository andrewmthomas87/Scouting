####  ####
Client to Server
```javascript
{

}
```
Server to Client
```javascript
{

}
```

# FRC Scouting App 2015 #
### JSON Messages ###
---
#### Log In ####
Client to Server
```javascript
{
    'MID': 0,
    'scoutName': String
}
```
Server to Client
```javascript
{
    'CID': int
}
```

#### Prepare for next match  ####
Client to Server
```javascript
{
    'MID': 1,
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

#### Ready for start ####
Client to Server
```javascript
{
    'MID': 2,
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


#### Contribution ####
Client to Server
```javascript
{
    'MID': 3,
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

#### Waz up? ####
Client to Server
```javascript
{
    'MID': 4,
    'CID': int
}
```
Server to Client
```javascript
{
    'updates': JSON
}
```

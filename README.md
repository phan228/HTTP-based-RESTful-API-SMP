# HTTP-based-RESTful-API-SMP

Name:  SMPDB. 


## Table Requirements:

Identity

    idnum INTEGER, PRIMARY KEY, // * see below
    handle VARCHAR(100), UNIQUE, // string beginning with @ symbol
    pass VARCHAR(100), // for project, store in clear is fine. [EDIT: changed 04/14]
    fullname VARCHAR(100), NOT NULL,
    location VARCHAR(100),
    email    VARCHAR(100), NOT NULL,
    bdate DATE NOT NULL,        // This person's birthdate
    joined DATE NOT NULL

Story

    sidnum INTEGER, PRIMARY KEY,
    idnum (Identity.idnum),
    chapter VARCHAR(100) NOT NULL, [EDIT 04/14: should be NOT NULL]
                                                    // some pithy saying someone might find amusing
    url VARCHAR(100),    // url to some picture
    expires DATETIME,    //     if not null, the date+time that 
                                                    //     this story expires and no longer 
                                                    //     appears on timelines
    tstamp TIMESTAMP  //     This is a datetime that fills in 
                                                    //     automatically on INSERT or UPDATE

Follows

    follower (Identity.idnum),  // person doing the following
    followed (Identity.idnum),  // who they're following
    tstamp TIMESTAMP

Reprint

    rpnum INTEGER, PRIMARY KEY,
    idnum (Identity.idnum),
    sidnum (Story.sidnum),    // original story
    likeit BOOLEAN,           // if you want to "like"
                                                     // someone's story
                                                     // true=like
                                                     // false=retweet
  [EDIT: 2020-04-02: I removed newstory]
    tstamp TIMESTAMP

Block

    blknum INTEGER, PRIMARY KEY,
    idnum (Identity.idnum),   // User
    blocked (Identity.idnum), // Person they want to block. 
                                                            // This person can't follow, nor 
                                                           // see Story, nor issue Reprint
    tstamp TIMESTAMP

* NOTE - for all the entity primary keys that are integers - use the BIGINT type with AUTO_INCREMENT.  This means when you INSERT a record, this field will be 
automatically assigned the next integer. It is 100% ACID compliant!

## APIs:

[04/14-EDIT: all credential errors should return the following:]

{"status_code":"-10", "error":"invalid credentials"}

### /api/createuser     // create a new Instatwitsnapbook user

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!", "fullname":"Angus Mize", "location":"Kentucky", "xmail":"none@nowhere.com", "bdate":"1970-07-01"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/createuser (Links to an external site.)

Output: {"status":"4"} // positive number is the Identity.idnum created.
Output: {"status":"-2", "error":"SQL Constraint Exception"}.


### /api/seeuser        // find a user and give information

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/seeuser/2 (Links to an external site.)

2 = Identity.idnum

Output: {"status":"1", "handle":"@carlos", "fullname":"Carlos Mize", "location":"Kentucky", "email":carlos@notgmail.com", "bdate":"1970-01-26","joined":"2020-04-01"}
Output: {}. // no match found, could be blocked, user doesn't know.

### /api/suggestions    // recommend (4) followers based on other followers

Query should be give idnum, handle of at most 4 (Hint: LIMIT 4) 

idnum and handles of people followed by people that are followed by you BUT not you and not anyone you already follow.

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/suggestions (Links to an external site.)

Output, status > 0 is the number of suggested people returned

Output: {"status":"3", "idnums":"1,2,4", "handles":"@paul,@carlos","@fake"}

Output: {"status":"0", "error":"no suggestions"}

### /api/poststory      // post a story/comment/etc and maybe picture

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!", "chapter":"I ate at Mario's!", "url":"http://imagesite.dne/marios.jpg"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/poststory (Links to an external site.)

Output: {"status":"1"} 

Output: {"status":"0", "error":"invalid expires date"}

[EDIT 04/22: this is erroneous, let the user do this, its their fault]
NO! Output: {"status":"0", "error":"expire date in past"}
Output: {"status":"0", "error":"missing chapter"}

### /api/reprint        // "like" or "retweet" someone's Story

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!", "likeit":true}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/reprint/45 (Links to an external site.)

if "likeit" is omitted, a coercion to boolean results in "false". 

FYI. Seems like reasonable result. [04/16]

45 = Story.sidnum

Output: {"status":"1"}

Output: {"status":"0", "error":"blocked"}

Output: {"status":"0", "error":"story not found"}

### /api/follow         // add someone to your followings list

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/follow/2 (Links to an external site.)

2 = Identity.idnum

Output: {"status":"1"}

Output: {"status":"0", "error":"blocked"}

DNE

### /api/unfollow       // remove someone from your followings list

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/unfollow/2 (Links to an external site.)

2 = Identity.idnum

Output: {"status":"1"}

Output: {"status":"0", "error":"not currently followed"}

### /api/block          // Block a user

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/block/2 (Links to an external site.)

2 = Identity.idnum

Output: {"status":"1"}

Output: {"status":"0", "error":"DNE"}

### /api/timeline       // see all Story/Reprints of people you follow for a particular time interval

Input: curl -d '{"handle":"@cooldude42", "password":"mysecret!", "newest":"2020-04-02 15:33:59", "oldest":"2020-03-29 00:00:01"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/timeline (Links to an external site.)

This is the most complicated API. I'll need a single SQL statement to get all the points for the rubric. This is a challenge to make the DBMS do all the hard work! 

My task is to create a single SQL query based on the requester's handle, producing all Story entries for all handles they follow, including any Reprint/retweets (ie, where Reprint.likeit=false). Only list those that have tstamps between the interval (older than "newest" and newer than "oldest" submitted). Assuming newest and oldest are valid values. I will have to deal with all tables, including the Block table. (if someone retweets a Story of someone that has blocked you, it should not show on your timeline).

[EDIT 04/16] Here is the output for timeline. Enumerate the key for each story/reprint

Then the value/righthand side will be a JSON object itself, curly brace, then (5) key/value pairs and a closing brace }. In the example code you can put this in a Map<String,String>. We're not worried about using this output, we just want to see it.

[EDIT 04/17] I left off sidnum, but it is needed.]

Output: {"0":"{\"type\":\"story\",\"author\":\"@cooldude44\",\"sidnum\":\"14\",\"chapter\":\"Just some set math, SQL is super fun!\",\"posted\":\"2020-04-16 15:37:48\"}","1":"{\"type\":\"reprint\",\"author\":\"@cooldude44\",\"sidnum\":\"15\",\"chapter\":\"JSON objects are fun and useful!\",\"posted\":\"2020-04-15 10:37:44\"}","status":"2"}

Output: {"status":"0"}


## Provided:

### /api/status

Test: curl http://localhost:9990/apt/status (Links to an external site.)

Output: {"status":"1"} 


### /api/exampleJSON

Test: curl -d '{"foo":"silly1", "bar":"silly2"}' -H "Content-Type: application/json" -X POST http://localhost:9990/api/exampleJSON (Links to an external site.)

Output: {"status":1, "foo":"silly1", "bar":silly2"}


### /api/exampleGETBDATE

Test: curl http://localhost:9990/api/exampleGETBDATE/1 (Links to an external site.)

Output: {"bdate":"1970-02-14"}

Note: this example makes a PreparedStatement SQL query.

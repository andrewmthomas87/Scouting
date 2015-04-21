
use scout

drop view if exists binnedStacks;
drop view if exists matchesPlayed;
drop view if exists teamMatches;

drop procedure if exists getHeightOfBinnedStacks;
drop procedure if exists getMaxHeightOfBinnedStacks;
drop procedure if exists getNumberOfBinnedStacksAtHeight;
drop procedure if exists getAutonomousBinRakers;


create view binnedStacks as select teamNumber, count(*) totes from contributions where object in ("F", "H") group by SID having SID in (select SID from contributions where object="B") order by totes desc, teamNumber asc;
create view teamMatches as
	select matchNumber, redTeam1 teamNumber, played from eventMatches
	union
	select matchNumber, redTeam2 teamNumber, played from eventMatches
	union
	select matchNumber, redTeam3 teamNumber, played from eventMatches
	union
	select matchNumber, blueTeam1 teamNumber, played from eventMatches
	union
	select matchNumber, blueTeam2 teamNumber, played from eventMatches
	union
	select matchNumber, blueTeam3 teamNumber, played from eventMatches;


create view matchesPlayed as select teamNumber, count(*) matches from teamMatches where played=1 group by teamNumber order by matches desc, teamNumber asc;

delimiter //


create procedure getHeightOfBinnedStacks()
	begin
		select * from binnedStacks;
	end;

create procedure getMaxHeightOfBinnedStacks()
	begin
		select teamNumber, max(totes) from binnedStacks group by teamNumber;
	end;

create procedure getNumberOfBinnedStacksAtHeight()
	begin
		select teamNumber, totes, count(*) from binnedStacks group by teamNumber, totes order by totes desc;
	end;

create procedure getAutonomousBinRakers()
	begin
		select teamNumber, count(*) rakes from robotEvents where eventType="R" group by teamNumber order by rakes desc, teamNumber asc;
	end;

//

delimiter ;

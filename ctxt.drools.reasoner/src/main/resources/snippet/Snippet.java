package snippet;

public class Snippet {
	rule "Null coincidence"
		when
			$e:NullActivity(room=="KITCHEN");
			NullActivity(room=="ALL", this coincides $e)
		then
			System.out.println("COINCIDENCE");
	end
	
	rule "Going to bed - From normal routine"
	salience 1
		when
			$a:NullActivity($r:room=="KITCHEN")
			NullActivity(room=="ALL", this coincides $a)
			//not ContextEvent( this after [1ms,30s] $a, ((ManagedIndividual)RDFObject).getURI() matches "http://ontology.universAAL.org/ActivityHub.owl#motion_detected" )
			//$ua:UserAwake()
			$uS:UserStatus(awake == true)
		then		
			modify($uS){setAwake(false)}
			//retract($ua);
			insert (new UserAsleep());
			RulesEngine.getInstance().publishConsequence(new String("http://www.tsbtecnologias.es/Consequence.owl#"+drools.getRule().getName()), new String[]{"ActivityType","Device"},new String[]{"GoingToBed","BEDROOM"});
			System.out.println("GOING TO BED"+" "+$r+" "+java.util.Calendar.getInstance().getTime());
	end
	
	rule "Awakening filter"
		when
		$sd:SleepDisturbance() 
		//Number($i:intValue == 1 ) from accumulate ($sd:SleepDisturbance() over window:time(20s),count($sd))
		
		
		//not (SleepDisturbance(this before[0ms,1m] $sd))
		//not (SleepDisturbance(this before[0ms,1m] $sd))
		then
		//Publish awakening
		System.out.println("There was an awakening");
		retract($sd);		
	end
	
	rule "Disturbance"
		when
			//$c:ContextEvent( getRoom(RDFSubject.getURI())=="KITCHEN" )
			$c: ContextEvent( getRoom(RDFSubject.getURI())=="KITCHEN"  , ((ManagedIndividual)RDFObject).getURI() matches "http://ontology.universAAL.org/ActivityHub.owl#motion_detected" )
			//not ContextEvent( this before [1ms,30s] $c)
			$na:NullActivity(room == "ALL", this after [1ms, 1h] $c)
			//$uS:UserStatus(awake == false)
			$ua:UserAsleep(this before [1ms, 10h] $c)
			//not (SleepDisturbance())
		then
			retract($na);
			//modify($uS){setAwake(true)}
			//RulesEngine.getInstance().publishConsequence(new String("http://www.tsbtecnologias.es/Consequence.owl#"+drools.getRule().getName()), new String[]{"ActivityType","Device"},new String[]{"Awakening","BEDROOM"});
			insert (new SleepDisturbance());
			//System.out.println("AWAKENING "+java.util.Calendar.getInstance().getTime());	
	end
	
	rule "Waking up"
		when
			$c:ContextEvent( getRoom(RDFSubject.getURI())=="KITCHEN" )
			//not ContextEvent( this before [1ms,30s] $c)
			not NullActivity(room == "ALL", this after [1ms, 1h] $c)
			$uS:UserStatus(awake == false)
		then
			modify($uS){setAwake(true)}
			RulesEngine.getInstance().publishConsequence(new String("http://www.tsbtecnologias.es/Consequence.owl#"+drools.getRule().getName()), new String[]{"ActivityType","Device"},new String[]{"WakingUp","BEDROOM"});
			insert(new SleepDisturbance());
			//System.out.println("WAKE UP "+java.util.Calendar.getInstance().getTime());
				
	end
}


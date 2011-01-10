CREATE DATABASE IF NOT EXISTS persona_aal_space;

-- The following two lines must be entered into a mysql console for each of the users
-- casf_che, casf_sr, prof_server and ui_dm, here the example for the latter one
--
-- CREATE USER 'ui_dm'@'localhost' IDENTIFIED BY 'ui_dm';
-- GRANT ALL PRIVILEGES ON persona_aal_space.* TO 'ui_dm'@'localhost';

SET FOREIGN_KEY_CHECKS=0;
use persona_aal_space;

DROP TABLE IF EXISTS jena_g1t0_reif;
DROP TABLE IF EXISTS jena_g1t1_stmt;
DROP TABLE IF EXISTS jena_graph;
DROP TABLE IF EXISTS jena_long_lit;
DROP TABLE IF EXISTS jena_long_uri;
DROP TABLE IF EXISTS jena_prefix;
DROP TABLE IF EXISTS jena_sys_stmt;

DROP TABLE IF EXISTS situation;
DROP TABLE IF EXISTS situation_index;
DROP FUNCTION notify_sr;
DROP PROCEDURE examine_situation_index;

DROP TABLE IF EXISTS ca_service_req;

--
-- The following drop trigger statements are not needed
-- because with the drop of the table, they are dropped, too
--
-- DROP TRIGGER sr_trigger_1;
-- DROP TRIGGER sr_trigger_2;
-- DROP TRIGGER sr_trigger_3;

CREATE TABLE jena_sys_stmt (
	Subj	varchar(100) NOT NULL,
	Prop	varchar(100) NOT NULL,
	Obj		varchar(100),
	GraphID	int(11),
	
	KEY jena_XSP (Subj, Prop),
	KEY jena_XO (Obj)
) ENGINE=InnoDB;

CREATE TABLE jena_prefix (
	ID		int(11) NOT NULL AUTO_INCREMENT,
	Head	varchar(100) NOT NULL,
	ChkSum	bigint(20),
	Tail	mediumblob,
	
	PRIMARY KEY (ID),
	UNIQUE KEY jena_XBND (Head, ChkSum)
) ENGINE=InnoDB;

CREATE TABLE jena_long_uri (
	ID		int(11) NOT NULL AUTO_INCREMENT,
	Head	varchar(100) NOT NULL,
	ChkSum	bigint(20),
	Tail	mediumblob,
	
	PRIMARY KEY (ID),
	UNIQUE KEY jena_XURI (Head, ChkSum)
) ENGINE=InnoDB;

CREATE TABLE jena_long_lit (
	ID		int(11) NOT NULL AUTO_INCREMENT,
	Head	varchar(100) NOT NULL,
	ChkSum	bigint(20),
	Tail	mediumblob,
	
	PRIMARY KEY (ID),
	UNIQUE KEY jena_XLIT (Head, ChkSum)
) ENGINE=InnoDB;

CREATE TABLE jena_graph (
	ID		int(11) NOT NULL AUTO_INCREMENT,
	Name	tinyblob,
	
	PRIMARY KEY (ID)
) ENGINE=InnoDB AUTO_INCREMENT=2;

CREATE TABLE jena_g1t0_reif (
	Subj	varchar(100),
	Prop	varchar(100),
	Obj		varchar(100),
	GraphID	int(11),
	Stmt	varchar(100) NOT NULL,
	HasType	char(1) NOT NULL,
	
	UNIQUE KEY jena_g1t0_reifXSTMT (Stmt, HasType),
	KEY jena_g1t0_reifXSP (Subj, Prop),
	KEY jena_g1t0_reifXO (Obj)
) ENGINE=InnoDB;

CREATE TABLE jena_g1t1_stmt (
	Subj	varchar(100) NOT NULL,
	Prop	varchar(100) NOT NULL,
	Obj		varchar(100) NOT NULL,
	GraphID	int(11),
	
	KEY jena_g1t1_stmtXSP (Subj, Prop),
	KEY jena_g1t1_stmtXO (Obj)
) ENGINE=InnoDB;

INSERT INTO jena_graph(ID, Name) VALUES(1, 'PERSONA_AAL_Space'); 

INSERT INTO jena_sys_stmt VALUES
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#EngineType:',
		'Lv:0::MySQL:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#DriverVersion:',
		'Lv:0::2.0alpha:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LayoutVersion:',
		'Lv:0::2.0:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#FormatDate:',
		'Lv:0::20081209T173924Z:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LongObjectLength:',
		'Lv:0::100:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#IndexKeyLength:',
		'Lv:0::100:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#IsTransactionDb:',
		'Lv:0::true:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#DoCompressURI:',
		'Lv:0::false:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#CompressURILength:',
		'Lv:0::100:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#TableNamePrefix:',
		'Lv:0::jena_:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7fff:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphName:',
		'Lv:0::JENA_DEFAULT',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7fff:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphType:',
		'Lv:0::generic:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7fff:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphId:',
		'Lv:0::0:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphName:',
		'Lv:0::PERSONA_AAL_Space:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphType:',
		'Lv:0::generic:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphId:',
		'Lv:0::1:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#StmtTable:',
		'Lv:0::jena_g1t1_stmt:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#ReifTable:',
		'Lv:0::jena_g1t0_reif:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__8000:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#Graph:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffd:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#PSetName:',
		'Lv:0::192_168_2_1102390acdb_11e1cd4eb9a__7ffc:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffd:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#PSetType:',
		'Lv:0::com.hp.hpl.jena.db.impl.PSet_ReifStore_RDB:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffd:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#PSetTable:',
		'Lv:0::jena_g1t0_reif:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffb:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LSetName:',
		'Lv:0::LSET_PERSONA_AAL_Space_REIFIER:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffb:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LSetType:',
		'Lv:0::com.hp.hpl.jena.db.impl.SpecializedGraphReifier_RDB:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffb:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LSetPSet:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffd:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphLSet:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffb:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffa:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#PSetName:',
		'Lv:0::192_168_2_1102390acdb_11e1cd4eb9a__7ff9:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffa:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#PSetType:',
		'Lv:0::com.hp.hpl.jena.db.impl.PSet_TripleStore_RDB:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffa:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#PSetTable:',
		'Lv:0::jena_g1t1_stmt:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ff8:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LSetName:',
		'Lv:0::LSET_PERSONA_AAL_Space:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ff8:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LSetType:',
		'Lv:0::com.hp.hpl.jena.db.impl.SpecializedGraph_TripleStore_RDB:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ff8:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#LSetPSet:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffa:',
		0),
	(
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ffe:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#GraphLSet:',
		'Uv::http://jena.hpl.hp.com/2003/04/DB#192_168_2_1102390acdb_11e1cd4eb9a__7ff8:',
		0);

CREATE TABLE situation (
    id        int(11) NOT NULL AUTO_INCREMENT,
    sit_query VARCHAR(2048) NOT NULL,
    
    PRIMARY KEY (id)

) ENGINE=InnoDB;

CREATE TABLE situation_index (

    situation		int(11) NOT NULL,
    ind_subj		varchar(100),
    ind_subj_type	varchar(100),
    ind_pred		varchar(100),

    FOREIGN KEY (situation) REFERENCES situation (id)
        ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

--
-- the column 'service_req' must contain a serialized ServiceRequest starting with '@prefix'
-- in the serialized request you can use the following constructs to refer to standard variables of the dialog manager
-- . ${subject} that will be replaced by the URI of the subject of the context event that triggers this service request
-- . ${subjectType} that will be replaced by the type-URI of the subject of the context event that triggers this service request
-- . ${predicate} that will be replaced by the URI of the predicate of the context event that triggers this service request
-- additionally, you can define your own variables using an ordered numbering; the variable definitions must precede the
-- serialized service request without any separator and must start with var# 0, continuing with var# 1, 2, ...
-- the definition of a variable with number 'i' (placeholder for 0, 1, 2, ... in te following text) must start with '${i='
-- and end with '=i}'; many var defs can follow each other without any separator; to such a variable can be referred by '${i}';
-- you can refer to the standard vars anywhere in such var defs; vars with a higher ordering number may refer to vars with a
-- lower ordering number, otherwise the order of var defs is not significant; if the string between '${i=' and '=i}' starts
-- with 'sparql:' the rest of it is used to make a sparql query to the central DB of the platform (the DB declared in this script);
-- if it starts with 'sql:' the rest of it is used to make a normal sql query to the same DB; otherwise, it will be used literally as the var value.
--
CREATE TABLE ca_service_req (

    service_req		varchar(4096) NOT NULL,
    ind_subj		varchar(100),
    ind_subj_type	varchar(100),
    ind_pred		varchar(100)

) ENGINE=InnoDB;

CREATE FUNCTION notify_sr RETURNS INTEGER SONAME 'persona_sr_notifier.dll';

DELIMITER //

CREATE PROCEDURE examine_situation_index(par_subj VARCHAR(100), par_pred VARCHAR(100))
BEGIN
	DECLARE query_str VARCHAR(4096);
	DECLARE dummy INT;
		
	IF par_pred != 'Uv::http://www.w3.org/1999/02/22-rdf-syntax-ns#type:' THEN
			
			SELECT s.sit_query INTO query_str
			  FROM situation s, situation_index si
			 WHERE si.situation = s.id
			   AND (si.ind_subj IS NULL OR si.ind_subj = par_subj)
			   AND (si.ind_pred IS NULL OR si.ind_pred = par_pred)
			   AND (si.ind_subj_type IS NULL OR si.ind_subj_type IN
						(SELECT Obj
						   FROM jena_g1t1_stmt
						  WHERE Prop = 'Uv::http://www.w3.org/1999/02/22-rdf-syntax-ns#type:'
						    AND Subj = par_subj)
			       )
			 LIMIT 1;
		   
			IF query_str IS NOT NULL THEN
				SELECT notify_sr(query_str) INTO dummy;
			END IF;

	END IF;
END //

DELIMITER ;

CREATE TRIGGER sr_trigger_1 AFTER DELETE ON jena_g1t1_stmt
	FOR EACH ROW CALL examine_situation_index(OLD.Subj, OLD.Prop);

CREATE TRIGGER sr_trigger_2 AFTER INSERT ON jena_g1t1_stmt
	FOR EACH ROW CALL examine_situation_index(NEW.Subj, NEW.Prop);

CREATE TRIGGER sr_trigger_3 AFTER UPDATE ON jena_g1t1_stmt
	FOR EACH ROW CALL examine_situation_index(NEW.Subj, NEW.Prop);

INSERT INTO situation VALUES
	(1, 'PREFIX hl: <urn:org.persona.aal_space:itaca_home_lab#>\
         PREFIX p: <http://ontology.aal-persona.org/PERSONA.owl#>\
		 PREFIX u: <http://ontology.persona.upm.es/User.owl#>\
         PREFIX a: <http://ontology.persona.ratio.it/action.owl#>\
		 PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\
         CONSTRUCT {\
            hl:livingRoom p:targetTemperature "21"^^xsd:int ;\
                          a p:PLocation . }\
		  WHERE {\
			?at a a:ActionType ;\
			   a:description "WatchingTV"^^xsd:string .\
			?a a a:CompositeAction ;\
               a:hasActionType ?at ;\
			   a:hasStatus "1"^^xsd:int .\
			?u a u:User ;\
               u:currentActivity ?a ;\
			   p:hasLocation hl:livingRoom .\
            hl:outside p:hasTemperature ?t1 .\
            hl:livingRoom p:hasTemperature ?t2 .\
            FILTER (?t1 < 10  &&  ?t2 < 21)\
		  }\
		'),
	(2, 'PREFIX p: <http://ontology.aal-persona.org/PERSONA.owl#>\
         PREFIX e: <http://ontology.ima.igd.fraunhofer.de/persona/HomeLab.owl#>\
         CONSTRUCT {\
            ?room e:entertainmentState ?state;\
                          a p:Room .
			?state a e:HomeEntertainmentState . }\
		  WHERE {\
			?u a <http://ontology.persona.upm.es/User.owl#User> ;\
			   <http://ontology.igd.fraunhofer.de/UserState.owl#hasSleepintState> ?asleep ;\
               <http://ontology.igd.fraunhofer.de/UserPosture.owl#hasPosture> ?lying ;\
			   p:hasLocation ?room .\
            FILTER ((?asleep = ss:asleep  &&  ?lying = up:lying  &&  ?state = e:rest)\
				||  (?asleep != ss:asleep  &&  ?lying = up:lying  &&  ?state = e:relaxation)\
				||  (?asleep = ss:asleep  &&  ?lying != up:lying  &&  ?state = e:unrecognized)\
				||  (?asleep != ss:asleep  &&  ?lying != up:lying  &&  ?state = e:entertainment))\
		  }\
		');
         
INSERT INTO situation_index VALUES
	(1,
		null,
		'Uv::http://ontology.persona.upm.es/User.owl#User:',
		'Uv::http://ontology.persona.upm.es/User.owl#currentActivity:'),
	(1, 
		'Uv::urn:org.persona.aal_space:itaca_home_lab#outside:',
		null,
		'Uv::http://ontology.aal-persona.org/PERSONA.owl#hasTemperature:'),
	(1,
		'Uv::urn:org.persona.aal_space:itaca_home_lab#livingRoom:',
		null,
		'Uv::http://ontology.aal-persona.org/PERSONA.owl#hasTemperature:'),
	(2,
		null,
		'Uv::http://ontology.persona.upm.es/User.owl#User:',
		'Uv::http://ontology.igd.fraunhofer.de/UserState.owl#hasSleepintState:'),
	(2,
		null,
		'Uv::http://ontology.persona.upm.es/User.owl#User:',
		'Uv::http://ontology.igd.fraunhofer.de/UserPosture.owl#hasPosture:');
	
--
-- See comments on ca_service_req
--
INSERT INTO ca_service_req VALUES (
    '${0=sparql:PREFIX p: <http://ontology.aal-persona.org/PERSONA.owl#>\nPREFIX b: <http://ontology.persona.ratio.it/DummyServiceProvider.owl#>\nSELECT ?b WHERE { ?b a b:Boiler ;\np:hasLocation <${subject}> . }=0}<?xml version="1.0"?><rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:j.0="http://www.daml.org/services/owl-s/1.1/Process.owl#" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns="http://ontology.aal-persona.org/PERSONA.owl#" xmlns:j.1="http://ontology.persona.ratio.it/DummyServiceProvider.owl#" xmlns:j.2="http://www.daml.org/services/owl-s/1.1/generic/Expression.owl#"><ServiceRequest><requestedService><j.1:DummyService><instanceLevelRestrictions rdf:parseType="Collection"><owl:Restriction><owl:onProperty rdf:resource="http://ontology.persona.ratio.it/DummyServiceProvider.owl#controlsBoiler"/><owl:hasValue><j.1:Boiler rdf:about="${0}"/></owl:hasValue></owl:Restriction></instanceLevelRestrictions><numberOfValueRestrictions rdf:datatype="http://www.w3.org/2001/XMLSchema#int">1</numberOfValueRestrictions></j.1:DummyService></requestedService><requiredResult><j.0:Result><j.0:hasEffect rdf:parseType="Collection"><j.2:Expression><j.2:expressionBody rdf:datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral">&lt;rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://ontology.aal-persona.org/PERSONA.owl#" xmlns:j.0="http://ontology.aal-persona.org/Service.owl#"&gt;&lt;j.0:ChangeEffect&gt;&lt;j.0:propertyValue rdf:datatype="http://www.w3.org/2001/XMLSchema#boolean"&gt;true&lt;/j.0:propertyValue&gt;&lt;j.0:affectedProperty&gt;&lt;j.0:PropertyPath&gt;&lt;j.0:thePath rdf:parseType="Collection"&gt;&lt;rdf:Description rdf:about="http://ontology.persona.ratio.it/DummyServiceProvider.owl#controlsBoiler"/&gt;&lt;rdf:Description rdf:about="http://ontology.persona.ratio.it/DummyServiceProvider.owl#isOn"/&gt;&lt;/j.0:thePath&gt;&lt;/j.0:PropertyPath&gt;&lt;/j.0:affectedProperty&gt;&lt;/j.0:ChangeEffect&gt;&lt;/rdf:RDF&gt;</j.2:expressionBody><j.2:expressionLanguage><j.2:LogicLanguage rdf:about="http://ontology.aal-persona.org/Service.owl#OWL"><j.2:refURI rdf:datatype="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2002/07/owl</j.2:refURI></j.2:LogicLanguage></j.2:expressionLanguage></j.2:Expression></j.0:hasEffect></j.0:Result></requiredResult></ServiceRequest></rdf:RDF>',
    null,
    'http://ontology.aal-persona.org/PERSONA.owl#PLocation',
    'http://ontology.aal-persona.org/PERSONA.owl#target_temperature');

--
-- A table used by profile service to keep the the Persona accounts (username, password) of the users
--

DROP TABLE IF EXISTS credentials;
CREATE TABLE  credentials (
  userURI varchar(255) NOT NULL,
  username varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  PRIMARY KEY (userURI),
  UNIQUE KEY username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

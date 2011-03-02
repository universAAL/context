CREATE DATABASE IF NOT EXISTS persona_aal_space;

-- The following two lines must be entered into a mysql console for each of the users
-- casf_che, casf_sr, prof_server and ui_dm, here the example for the latter one
--
-- CREATE USER 'ui_dm'@'localhost' IDENTIFIED BY 'ui_dm';
-- GRANT ALL PRIVILEGES ON persona_aal_space.* TO 'ui_dm'@'localhost';

SET FOREIGN_KEY_CHECKS=0;
use universaal_history;

DROP TABLE IF EXISTS situation;
DROP TABLE IF EXISTS situation_index;
DROP FUNCTION notify_sr;
DROP PROCEDURE examine_situation_index;

DROP TRIGGER sr_trigger_u1;
DROP TRIGGER sr_trigger_u2;
DROP TRIGGER sr_trigger_u3;

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

CREATE TRIGGER sr_trigger_u1 AFTER DELETE ON jena_g1t1_stmt
	FOR EACH ROW CALL examine_situation_index(OLD.Subj, OLD.Prop);

CREATE TRIGGER sr_trigger_u2 AFTER INSERT ON jena_g1t1_stmt
	FOR EACH ROW CALL examine_situation_index(NEW.Subj, NEW.Prop);

CREATE TRIGGER sr_trigger_u3 AFTER UPDATE ON jena_g1t1_stmt
	FOR EACH ROW CALL examine_situation_index(NEW.Subj, NEW.Prop);

INSERT INTO situation VALUES
	(1, 'PREFIX uaal: <http://ontology.universAAL.org/uAAL.owl#>
PREFIX device: <http://ontology.universAAL.org/Device.owl#>
PREFIX prof: <http://ontology.persona.upm.es/User.owl#>
CONSTRUCT { ?usr uaal:hasLocation ?loc ;
		 a ?typ .}
WHERE {
    ?dev a device:PanicButton ;
         uaal:isPortable true ;
         uaal:hasLocation ?loc ;
         uaal:carriedBy ?usr .
    ?usr a ?typ .
}
		');
         
INSERT INTO situation_index VALUES
	(1,
		null,
		null,
		'Uv::http://ontology.universAAL.org/uAAL.owl#hasLocation:');

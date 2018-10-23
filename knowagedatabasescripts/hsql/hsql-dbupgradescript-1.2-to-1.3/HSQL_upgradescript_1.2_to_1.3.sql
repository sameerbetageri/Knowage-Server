ALTER TABLE SBI_META_MODELS_VERSIONS ALTER COLUMN CONTENT LONGVARBINARY DEFAULT NULL;

ALTER TABLE SBI_META_MODELS_VERSIONS ADD FILE_MODEL LONGVARBINARY NOT NULL;

ALTER TABLE SBI_OBJ_METACONTENTS ADD ADDITIONAL_INFO VARCHAR(255) DEFAULT NULL;

ALTER TABLE SBI_CATALOG_FUNCTION  ADD OWNER VARCHAR(50) NOT NULL;
ALTER TABLE SBI_CATALOG_FUNCTION  ADD KEYWORDS VARCHAR(255) DEFAULT NULL;

ALTER TABLE SBI_CATALOG_FUNCTION ADD LABEL VARCHAR(50) NOT NULL;

ALTER TABLE SBI_CATALOG_FUNCTION ADD CONSTRAINT UNIQUE_LABEL_ORG UNIQUE (LABEL,ORGANIZATION);

ALTER TABLE SBI_CATALOG_FUNCTION ADD TYPE VARCHAR(50) DEFAULT NULL;

ALTER TABLE SBI_DATA_SET DROP COLUMN IS_PUBLIC;

CREATE MEMORY TABLE SBI_WHATIF_WORKFLOW(ID INTEGER,MODEL_ID INTEGER,USER_ID INTEGER,SEQUENCE INTEGER,STATE VARCHAR(20),NOTES VARCHAR(100),INFO VARCHAR(100),USER_IN VARCHAR(100),USER_UP VARCHAR(100),USER_DE VARCHAR(100),TIME_IN TIMESTAMP,TIME_UP TIMESTAMP,TIME_DE TIMESTAMP,SBI_VERSION_IN VARCHAR(10),SBI_VERSION_UP VARCHAR(10),SBI_VERSION_DE VARCHAR(10),ORGANIZATION VARCHAR(20), CONSTRAINT XPKSBI_WHATIF_WORKFLOW PRIMARY KEY (ID));
ALTER TABLE SBI_WHATIF_WORKFLOW ADD CONSTRAINT FK_SBI_MODEL_WORKFLOW FOREIGN KEY (MODEL_ID) REFERENCES SBI_ARTIFACTS(ID)  ON DELETE CASCADE;
ALTER TABLE SBI_WHATIF_WORKFLOW ADD CONSTRAINT FK_SBI_USER_WORKFLOW	FOREIGN KEY (USER_ID) REFERENCES SBI_USER(ID) ON DELETE CASCADE;
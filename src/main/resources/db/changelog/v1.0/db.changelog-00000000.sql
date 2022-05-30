--liquibase formatted sql


--changeset ibardych:0000000000042-1
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;

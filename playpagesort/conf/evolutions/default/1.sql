# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table gom_conversation (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_gom_conversation primary key (id))
;

create table gom_event (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  time                      timestamp,
  conversation_id           bigint,
  content                   varchar(255),
  constraint pk_gom_event primary key (id))
;

create sequence gom_conversation_seq;

create sequence gom_event_seq;

alter table gom_event add constraint fk_gom_event_conversation_1 foreign key (conversation_id) references gom_conversation (id) on delete restrict on update restrict;
create index ix_gom_event_conversation_1 on gom_event (conversation_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists gom_conversation;

drop table if exists gom_event;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists gom_conversation_seq;

drop sequence if exists gom_event_seq;


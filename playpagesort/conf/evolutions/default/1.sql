# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table gom_conversation (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  comment                   varchar(255),
  is_active                 tinyint(1) default 0,
  constraint pk_gom_conversation primary key (id))
;

create table gom_event (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  time                      datetime(6),
  conversation_id           bigint,
  content                   varchar(255),
  constraint pk_gom_event primary key (id))
;

alter table gom_event add constraint fk_gom_event_conversation_1 foreign key (conversation_id) references gom_conversation (id) on delete restrict on update restrict;
create index ix_gom_event_conversation_1 on gom_event (conversation_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table gom_conversation;

drop table gom_event;

SET FOREIGN_KEY_CHECKS=1;


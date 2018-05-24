drop table T_ORDER cascade constraints;
drop table T_ORDER_0 cascade constraints;
drop table T_ORDER_1 cascade constraints;
drop table T_ORDER_ITEM cascade constraints;
drop table T_ORDER_ITEM_0 cascade constraints;
drop table T_ORDER_ITEM_1 cascade constraints;

-- Create table
create table T_ORDER
(
  order_seq_id varchar2(64),
  order_id varchar2(64),
  user_id  varchar2(64),
  status varchar2(50),
  create_date  DATE
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table T_ORDER
  add constraint ORDER_ID_PK primary key (order_seq_id);
  
-- Create table
create table T_ORDER_item
(
  item_id  varchar2(64),
  order_seq_id varchar2(64),
  order_id varchar2(64),
  user_id  varchar2(64),
  create_date  DATE
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table T_ORDER_item
  add constraint ORDER_item_ID_PK primary key (item_id);


-- Create table
create table T_ORDER_0
(
  order_seq_id varchar2(64),
  order_id varchar2(64),
  user_id  varchar2(64),
  status varchar2(50),
  create_date  DATE
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table T_ORDER_0
  add constraint ORDER_0_ID_PK primary key (order_seq_id);
  
-- Create table
create table T_ORDER_item_0
(
  item_id varchar2(64),
  order_seq_id varchar2(64),
  order_id varchar2(64),
  user_id  varchar2(64),
  create_date  DATE
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table T_ORDER_item_0
  add constraint ORDER_item_0_ID_PK primary key (item_id);
  
-- Create table
create table T_ORDER_1
(
  order_seq_id varchar2(64),
  order_id varchar2(64),
  user_id  varchar2(64),
  status varchar2(50),
  create_date  DATE
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table T_ORDER_1
  add constraint ORDER_1_ID_PK primary key (order_seq_id);
  
-- Create table
create table T_ORDER_item_1
(
  item_id varchar2(64),
  order_seq_id varchar2(64),
  order_id varchar2(64),
  user_id  varchar2(64),
  create_date  DATE
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table T_ORDER_item_1
  add constraint ORDER_item_1_ID_PK primary key (item_id);

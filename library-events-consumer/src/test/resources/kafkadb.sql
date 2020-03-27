create schema if not exists kafkadb collate latin1_swedish_ci;

create table if not exists library_event
(
	library_event_id int auto_increment
		primary key,
	library_event_type int null
);

create table if not exists book
(
	book_id int not null
		primary key,
	book_author varchar(255) null,
	book_name varchar(255) null,
	library_event_id int null,
	constraint FK6qi3i8pq7m43mhr7pqgu22teo
		foreign key (library_event_id) references library_event (library_event_id)
);

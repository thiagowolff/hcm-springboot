insert into user (user_id, username, password, name, role, active, notification_messages, notification_sounds) values (1, 'admin', 'jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=', 'Test User', 'ADMIN', 1, 0, 0);

insert into chamber (chamber_id, name, capacity) values (1, 'Câmara I', 9);
insert into chamber (chamber_id, name, capacity) values (2, 'Câmara II', 9);

insert into event_type(event_type_id, event_type_code, description, session_status) values (1, 'START', 'Início', 'COMPRESSING');
insert into event_type(event_type_id, event_type_code, description, session_status) values (2, 'WEAR_MASK', 'Colocar máscaras', 'O2_ON');
insert into event_type(event_type_id, event_type_code, description, session_status) values (3, 'REMOVE_MASK', 'Retirar máscaras', 'O2_OFF');
insert into event_type(event_type_id, event_type_code, description, session_status) values (4, 'SHUTDOWN', 'Descompressão', 'SHUTTING_DOWN');
insert into event_type(event_type_id, event_type_code, description, session_status) values (5, 'COMPLETION', 'Término', 'FINISHED');

insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 0, 1);
insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 10, 2);
insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 20, 3);
insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 40, 2);
insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 60, 3);
insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 80, 2);
insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 90, 4);
insert into chamber_event (chamber_id, timeout, event_type_id) values (1, 100, 5);

insert into patient (patient_id, name, patient_record, birth_date, gender, active) values (1, 'Patient X', 'X001', date '1980-01-01', 'M', 1);
insert into patient (patient_id, name, patient_record, birth_date, gender, active) values (2, 'Patient Y', 'Y002', date '1990-01-01', 'M', 1);
insert into patient (patient_id, name, patient_record, birth_date, gender, active) values (3, 'Patient Z', 'Z003', date '2000-01-01', 'M', 1);

insert into session (session_id, chamber_id, scheduled_time, start_time, end_time, status, execution_metadata, created_on, created_by) values (1, 1, timestamp '2017-01-01 10:00:00', time '10:00:00', time '10:01:10', 'CREATED', null, now(), 1);

insert into patient_session (patient_id, session_id, absent) values (1, 1, 0);
insert into patient_session (patient_id, session_id, absent) values (2, 1, 0);


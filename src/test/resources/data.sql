insert into user (user_id, username, password, name, role, active, notification_messages, notification_sounds) values (1, 'admin', 'jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=', 'Test User', 'ADMIN', 1, 0, 0);

insert into chamber (chamber_id, name, capacity) values (1, 'Câmara I', 9);
insert into chamber (chamber_id, name, capacity) values (2, 'Câmara II', 9);

insert into chamber_event (chamber_id, timeout, event_type) values (1, 0,' START');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 10, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 20, 'REMOVE_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 40, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 60, 'REMOVE_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 80, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 90, 'SHUTDOWN');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 100, 'COMPLETION');

insert into patient (patient_id, name, patient_record, active) values (1, 'Patient X', 'X001', 1);
insert into patient (patient_id, name, patient_record, active) values (2, 'Patient Y', 'Y002', 1);
insert into patient (patient_id, name, patient_record, active) values (3, 'Patient Z', 'Z003', 1);

insert into session (session_id, chamber_id, scheduled_time, start_time, end_time, status, execution_metadata, created_on, created_by) values (1, 1, timestamp '2017-01-01 10:00:00', time '10:00:00', time '10:01:10', 'CREATED', null, now(), 1);

insert into patient_session (patient_id, session_id, absent) values (1, 1, 0);
insert into patient_session (patient_id, session_id, absent) values (2, 1, 0);


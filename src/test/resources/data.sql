insert into chamber (chamber_id, name, capacity) values (1, 'Câmara II', 9);
insert into chamber (chamber_id, name, capacity) values (2, 'Câmara II', 9);

insert into chamber_event (chamber_id, timeout, event_type) values (1, 0,' CREATION');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 0,' START');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 10, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 20, 'REMOVE_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 30, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 40, 'REMOVE_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 50, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 60, 'SHUTDOWN');
insert into chamber_event (chamber_id, timeout, event_type) values (1, 70, 'COMPLETION');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 0,' CREATION');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 0,' START');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 10, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 20, 'REMOVE_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 30, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 40, 'REMOVE_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 50, 'WEAR_MASK');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 60, 'SHUTDOWN');
insert into chamber_event (chamber_id, timeout, event_type) values (2, 70, 'COMPLETION');

insert into patient (patient_id, name, patient_record, active) values (1, 'Patient X', 'X001', 1);
insert into patient (patient_id, name, patient_record, active) values (2, 'Patient Y', 'Y002', 1);
insert into patient (patient_id, name, patient_record, active) values (3, 'Patient Z', 'Z002', 1);

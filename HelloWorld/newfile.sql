
/* Drop Tables */

DROP TABLE Board CASCADE CONSTRAINTS;
DROP TABLE member CASCADE CONSTRAINTS;




/* Create Tables */

CREATE TABLE Board
(
	num  NOT NULL,
	title varchar2(200) NOT NULL,
	content varchar2(2000) NOT NULL,
	-- board의 primary key
	id varchar2(30) NOT NULL,
	postdate date NOT NULL,
	visitcount varchar2(0) NOT NULL,
	PRIMARY KEY (num)
);


CREATE TABLE member
(
	-- board의 primary key
	id varchar2(30) NOT NULL,
	pass varchar2(30) NOT NULL,
	name varchar2(30) NOT NULL,
	-- 기본값(defalult)으로 sysdate지정
	regidate date NOT NULL,
	PRIMARY KEY (id)
);



/* Create Foreign Keys */

ALTER TABLE Board
	ADD FOREIGN KEY (id)
	REFERENCES member (id)
;



/* Comments */

COMMENT ON COLUMN Board.id IS 'board의 primary key';
COMMENT ON COLUMN member.id IS 'board의 primary key';
COMMENT ON COLUMN member.regidate IS '기본값(defalult)으로 sysdate지정';




--
-- PostgreSQL database dump
--

\restrict JbryJyMbi6dhNLYthxttY4EfMZO3hWo5awassBWS9sMc49InMLBc6ovYaGLrNAk

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: delivery_addresses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_addresses (
    removed boolean,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    id uuid NOT NULL,
    user_id uuid,
    apartment character varying(255),
    city character varying(255) NOT NULL,
    entrance character varying(255),
    floor character varying(255),
    house character varying(255) NOT NULL,
    street character varying(255) NOT NULL
);


ALTER TABLE public.delivery_addresses OWNER TO postgres;

--
-- Name: order_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_items (
    price_at_purchase numeric(10,2) NOT NULL,
    quantity integer NOT NULL,
    removed boolean,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    product_id uuid NOT NULL
);


ALTER TABLE public.order_items OWNER TO postgres;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.orders (
    removed boolean,
    total_price numeric(10,2) NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    address_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid,
    comment character varying(255),
    customer_name character varying(255),
    customer_phone character varying(255) NOT NULL,
    payment_type character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT orders_payment_type_check CHECK (((payment_type)::text = ANY ((ARRAY['CARD'::character varying, 'CASH'::character varying, 'ONLINE'::character varying])::text[]))),
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PAID'::character varying, 'ASSEMBLED'::character varying, 'SHIPPED'::character varying, 'DELIVERED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.orders OWNER TO postgres;

--
-- Name: products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products (
    price numeric(10,2) NOT NULL,
    removed boolean,
    sub_category smallint,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    id uuid NOT NULL,
    category character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    picture_url character varying(255),
    CONSTRAINT products_category_check CHECK (((category)::text = ANY ((ARRAY['DIARY'::character varying, 'BAKERY'::character varying, 'DRINKS'::character varying, 'SNACKS'::character varying, 'MEAT'::character varying, 'GROCERY'::character varying, 'VEGETABLES'::character varying, 'FREEZINGS'::character varying, 'DISHES'::character varying])::text[]))),
    CONSTRAINT products_sub_category_check CHECK (((sub_category >= 0) AND (sub_category <= '-1'::integer)))
);


ALTER TABLE public.products OWNER TO postgres;

--
-- Name: stock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stock (
    purchase_price numeric(38,2) NOT NULL,
    quantity integer NOT NULL,
    removed boolean,
    reserved_quantity integer NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    version bigint NOT NULL,
    id uuid NOT NULL,
    product_id uuid NOT NULL
);


ALTER TABLE public.stock OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    removed boolean,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    id uuid NOT NULL,
    email character varying(255),
    first_name character varying(255) NOT NULL,
    last_name character varying(255),
    phone character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['SYSTEM_ADMIN'::character varying, 'MANAGER'::character varying, 'CLIENT'::character varying, 'WAREHOUSE_STUFF'::character varying, 'COURIER'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: delivery_addresses delivery_addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_addresses
    ADD CONSTRAINT delivery_addresses_pkey PRIMARY KEY (id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- Name: orders orders_address_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_address_id_key UNIQUE (address_id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: products products_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_name_key UNIQUE (name);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: stock stock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_pkey PRIMARY KEY (id);


--
-- Name: stock stock_product_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_product_id_key UNIQUE (product_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_phone_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_key UNIQUE (phone);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: orders fk32ql8ubntj5uh44ph9659tiih; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk32ql8ubntj5uh44ph9659tiih FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: delivery_addresses fk5lv80e9vimtc9usl7x4sfbnpn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_addresses
    ADD CONSTRAINT fk5lv80e9vimtc9usl7x4sfbnpn FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: order_items fkbioxgbv59vetrxe0ejfubep1w; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT fkbioxgbv59vetrxe0ejfubep1w FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: stock fkeuiihog7wq4cu7nvqu7jx57d2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT fkeuiihog7wq4cu7nvqu7jx57d2 FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: orders fki7qso7s625n7aarbahpyug1hf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fki7qso7s625n7aarbahpyug1hf FOREIGN KEY (address_id) REFERENCES public.delivery_addresses(id);


--
-- Name: order_items fkocimc7dtr037rh4ls4l95nlfi; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT fkocimc7dtr037rh4ls4l95nlfi FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- PostgreSQL database dump complete
--

\unrestrict JbryJyMbi6dhNLYthxttY4EfMZO3hWo5awassBWS9sMc49InMLBc6ovYaGLrNAk


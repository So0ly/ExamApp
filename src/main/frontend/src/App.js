import React from 'react';
import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom';
import {Login, Register} from './auth';
import {Examiners} from './examiners';
import {Reports, ReportsNew} from './reports'
import {Exam} from "./exam";
import {LandingPage} from "./LandingPage";

export const App = () => (
    <BrowserRouter>
      <Routes>
        <Route exact path='/reports' element={<Reports/>}/>
        <Route exact path='/reports/new' element={<ReportsNew/>}/>
        <Route exact path='/' element={<Navigate to='/reports' />} />
        <Route exact path='/login' element={<Login />} />
        <Route exact path='/register' element={<Register />} />
        <Route exact path='/examiners' element={<Examiners />} />
        <Route exact path='/exam' element={<Exam />} />
        <Route exact path='/landing' element={<LandingPage />} />
        <Route path="*" element={<Navigate to="/landing"/>}/>
      </Routes>
    </BrowserRouter>
);
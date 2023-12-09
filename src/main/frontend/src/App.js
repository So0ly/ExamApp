import React from 'react';
import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom';
import {Login} from './auth';
import {Examiners} from './examiners';
import {Reports, ReportsNew} from './reports'

export const App = () => (
    <BrowserRouter>
      <Routes>
        <Route exact path='/reports' element={<Reports/>}/>
        <Route exact path='/reports/new' element={<ReportsNew/>}/>
        <Route exact path='/' element={<Navigate to='/reports' />} />
        <Route exact path='/login' element={<Login />} />
        <Route exact path='/examiners' element={<Examiners />} />
        <Route path="*" element={<Navigate to="/login"/>}/>
      </Routes>
    </BrowserRouter>
);
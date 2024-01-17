import {combineReducers, configureStore} from '@reduxjs/toolkit';
import {logout, authReducer} from './auth';
import {layoutReducer} from './layout';
import {examReducer} from './exam';
import {examinerApi} from './examiners';
import {reportApi} from './reports';
import {studentApi} from "./students";


const appReducer = combineReducers({
    auth: authReducer,
    layout: layoutReducer,
    exam: examReducer,
    [examinerApi.reducerPath]: examinerApi.reducer,
    [reportApi.reducerPath]: reportApi.reducer,
    [studentApi.reducerPath]: studentApi.reducer
});

const rootReducer = (state, action) => {
    if (logout.match(action)) {
        state = undefined;
    }
    return appReducer(state, action);
};

export const store = configureStore({
    reducer: rootReducer,
    middleware: getDefaultMiddleware => getDefaultMiddleware({
        serializableCheck: {
            ignoredActions: ['reports/executeQuery/fulfilled'], // Add ignored actions here
        },
    })
        .concat(examinerApi.middleware)
        .concat(reportApi.middleware)
        .concat(studentApi.middleware)
});
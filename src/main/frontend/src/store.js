import {combineReducers, configureStore} from '@reduxjs/toolkit';
import {logout, reducer as authReducer} from './auth';
import {reducer as layoutReducer} from './layout';
import {api as examinerApi} from './examiners';

const appReducer = combineReducers({
    auth: authReducer,
    layout: layoutReducer,
    [examinerApi.reducerPath]: examinerApi.reducer
});

const rootReducer = (state, action) => {
    if (logout.match(action)) {
        state = undefined;
    }
    return appReducer(state, action);
};

export const store = configureStore({
    reducer: rootReducer,
    middleware: getDefaultMiddleware => getDefaultMiddleware()
        .concat(examinerApi.middleware)
});
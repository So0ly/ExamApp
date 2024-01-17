import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import {fetchBaseQuery} from '@reduxjs/toolkit/dist/query/react';
import Cookies from 'js-cookie';

export const login = createAsyncThunk(
    'auth/login',
    async ({mail, password}, thunkAPI) => {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({mail, password}),
        });
        if (!response.ok) {
            return thunkAPI.rejectWithValue({
                status: response.status, statusText: response.statusText, data: response.data});
        }
        return response.text();
    }
);

export const register = createAsyncThunk(
    'auth/register',
    async ({mail, password, firstName, lastName, titles}, thunkAPI) => {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({mail, password, firstName, lastName, titles}),
        });
        if (!response.ok) {
            return thunkAPI.rejectWithValue({
                status: response.status, statusText: response.statusText, data: response.data});
        }
        return response.text();
    }
);

const authSlice = createSlice({
    name: 'auth',
    initialState: {
        jwt: Cookies.get('jwt')
    },
    reducers: {
        logout: state => {
            Cookies.remove("jwt");
            state.jwt = null;
        }
    },
    extraReducers: {
        [login.fulfilled]: (state, action) => {
            Cookies.set('jwt', action.payload, { expires: 1/8 });
            state.jwt = action.payload;
        }
    }
});

export const {logout} = authSlice.actions;
export const {reducer} = authSlice;

export const authBaseQuery = ({path}) => {
    const baseQuery = fetchBaseQuery({
        baseUrl: `${process.env.REACT_APP_API_URL}/${path}`,
        prepareHeaders: (headers, {getState}) => {
            headers.set('authorization', `Bearer ${getState().auth.jwt}`);
            return headers;
        }
    });
    return async (args, api, extraOptions) => {
        const result = await baseQuery(args, api, extraOptions);
        if (result.error && result.error.status === 401) {
            api.dispatch(logout());
        }
        return result;
    };
};

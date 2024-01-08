import {createApi} from '@reduxjs/toolkit/query/react';
import {authBaseQuery} from '../auth';

export const studentApi = createApi({
    reducerPath: 'student',
    baseQuery: authBaseQuery({path: 'student'}),
    tagTypes: ['Student'],
    endpoints: builder => ({
        getAllStudents: builder.query({
            query: () => '/',
            providesTags: ['Student'],
        }),
        getStudent: builder.query({
            query: id => `/${id}`,
            providesTags: ['Student'],
        }),
        getStudentByIndex: builder.query({
            query: index => `/index/${index}`,
            providesTags: ['Student'],
        }),
        studentCSV: builder.mutation({
            query: file => ({
                url: `/file`,
                method: 'POST',
                body: file
            }),
            invalidatesTags: ['Student']
        })
    })
});
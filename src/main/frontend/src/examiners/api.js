import {createApi} from '@reduxjs/toolkit/query/react';
import {authBaseQuery} from '../auth';

export const examinerApi = createApi({
    reducerPath: 'examiners',
    baseQuery: authBaseQuery({path: 'examiner'}),
    tagTypes: ['Examiner'],
    endpoints: builder => ({
        getExaminer: builder.query({
            query: id => `/${id}`,
            providesTags: ['Examiner']
        }),
        getExaminers: builder.query({
            query: () => '/',
            providesTags: ['Examiner']
        }),
        deleteExaminer: builder.mutation({
            query: examiner => ({
                url: `/${examiner.id}`,
                method: 'DELETE'
            }),
            invalidatesTags: ['Examiner']
        }),
        getSelf: builder.query({
            query: () => '/self',
            providesTags: ['Examiner']
        }),
        changePassword: builder.mutation({
            query: passwordChange => ({
                url: `/self/password`,
                method: 'PUT',
                body: passwordChange
            })
        })
    })
});
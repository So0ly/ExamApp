import {createApi} from '@reduxjs/toolkit/query/react';
import {authBaseQuery} from '../auth';

export const reportApi = createApi({
    reducerPath: 'reports',
    baseQuery: authBaseQuery({path: 'report'}),
    tagTypes: ['Report'],
    endpoints: builder => ({
        getReports: builder.query({
            query: () => '/',
            providesTags: ['Report'],
        }),
        getAllReports: builder.query({
            query: () => '/all',
            providesTags: ['Report'],
        }),
        addReport: builder.mutation({
            query: report => ({
                url: '/',
                method: 'POST',
                body: report
            }),
            invalidatesTags: ['Report'],
        }),
        deleteReport: builder.mutation({
            query: report => ({
                url: `/${report.id}`,
                method: 'DELETE'
            }),
            invalidatesTags: ['Report']
        }),
        updateReport: builder.mutation({
            query: report => ({
                url: `/${report.id}`,
                method: 'PUT',
                body: report
            }),
            invalidatesTags: ['Report'],
        }),
        generateReport: builder.mutation({
            query: report => ({
                url: `/generate`,
                method: 'POST',
                body: report
            }),
            invalidatesTags: ['Report'],
        }),
        getPDF: builder.query({
            query: filename => `/pdf/${filename}`,
        }),
        questionCSV: builder.mutation({
            query: file => ({
                url: `/file`,
                method: 'POST',
                body: file
            })
        })
    })
});
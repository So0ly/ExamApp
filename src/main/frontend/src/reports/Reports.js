import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {
    Container,
    IconButton,
    Paper,
    Table,
    TablePagination,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Typography,
    TableSortLabel, Checkbox, Button, Grid
} from '@mui/material';
import {Delete, ExpandLess, ExpandMore} from '@mui/icons-material/';
import { reportApi } from './api';
import { Layout } from '../layout';
import {examinerApi} from "../examiners";
import { format } from "date-fns"
import {MaterialReactTable, useMaterialReactTable} from "material-react-table";

export const Reports = () => {
    const {data: userData} = examinerApi.endpoints.getSelf.useQuery();
    const {data: allReports} = reportApi.endpoints.getAllReports.useQuery(undefined);
    const {data: reports} = reportApi.endpoints.getReports.useQuery(undefined);
    const [deleteReport] = reportApi.endpoints.deleteReport.useMutation();
    const [generateReport] = reportApi.endpoints.generateReport.useMutation();
    const [getPDF] = reportApi.endpoints.getPDF.useLazyQuery();
    const [getAudio] = reportApi.endpoints.getAudio.useLazyQuery();

    const [rowSelection, setRowSelection] = useState({});

    const isAdmin = userData && userData.roles.includes('admin');

    const columns = useMemo(
        () => {
            const baseColumns =[
                {accessorKey: 'className', header: 'Nazwa przedmiotu'},
                {accessorKey: 'examDate', header: 'Data zaliczenia'},
                {accessorKey: 'studentName', header: 'Student'},
                {accessorKey: 'finalGrade', header: 'Ocena końcowa'},
                {accessorKey: 'examDuration', header: 'Czas trwania'}
            ];
            // baseColumns.unshift({
            //     id: 'expand',
            //     Header: 'Nagranie',
            //     Cell: ({ row }) => (
            //         <button onClick={handleAudioButtonClick(row.)}>
            //             {row.isExpanded ? 'Collapse' : 'Expand'}
            //         </button>
            //     ),
            // });
            if(isAdmin){
                baseColumns.push({accessorKey: 'examinerName', header: 'Egzaminator'},)
            }
            return baseColumns;
        }, [isAdmin]
    );

    const currentData = isAdmin ? allReports ?? [] : reports ?? [];

    useEffect(() => {
        // console.log(currentData)
    }, [currentData]);

    useEffect(() => {
    }, [rowSelection]);

    // const renderSubComponent = useCallback(
    //     (row) => {
    //         return (
    //             <div>
    //                 {row.original.questions && Object.entries(row.original.questions).map(([question, grade], index) => (
    //                     <div key={index}>
    //                         <strong>Question:</strong> {question} - <strong>Grade:</strong> {grade}
    //                     </div>
    //                 ))}
    //             </div>
    //         );
    //     },
    //     [] // dependencies, if any
    // );


    const data = useMemo(() => {
        return currentData
            ? currentData.map(report => ({
                ...report,
                studentName: `${report.student.firstName} ${report.student.lastName}`,
                examinerName: `${report.examiner.titles} ${report.examiner.firstName} ${report.examiner.lastName}`,
                examDate: format(new Date(report.examDate), 'yyyy-MM-dd'),
            }))
            : [];
    }, [currentData]);

    const tableInitialState = useMemo( () => ({
        grouping: ['className', 'examDate'], //an array of columns to group by by default (can be multiple)
        sorting:  [{ id: 'className', desc: true }, { id: 'examDate', desc: true }], //an array of columns to group by by default (can be multiple)
        pagination: { pageIndex: 0, pageSize: 20 },
        expanded: true,
    }), []);

    const reportTable = useMaterialReactTable({
        columns,
        data: data,
        enableRowSelection: true,
        enableMultiRowSelection: true,
        enableSubRowSelection: true,
        enableGrouping: true,
        groupedColumnMode: 'reorder',
        initialState: tableInitialState,
        enableSorting: true,
        enableExpandAll: true,
        enableCollapseAll: true,
        getRowId: (originalRow) => originalRow.id,
        // getSubRows: (originalRow) => originalRow.questions,
        onRowSelectionChange: setRowSelection,
        state: {rowSelection},
        // renderSubComponent: renderSubComponent
    });

    const handleReportButtonClick = async () => {
        try {
            const numericIds = Object.keys(rowSelection).filter(key => !isNaN(+key) && rowSelection[key]);
            const response = await generateReport({ ids: numericIds }).unwrap();
            const fileResponse = await getPDF(response.url);
            const blobby = fileResponse.data;
            const fileURL = window.URL.createObjectURL(blobby);
            const link = document.createElement('a');
            link.href = fileURL;
            link.setAttribute('download', response.url);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(fileURL);
        } catch (error) {
            console.error("Failed to generate report: ", error);
        }
    };

    const handleAudioButtonClick = async (fileName) => {
        try {
            const fileResponse = await getAudio(fileName);
            const blobby = fileResponse.data;
            const fileURL = window.URL.createObjectURL(blobby);
            const link = document.createElement('a');
            link.href = fileURL;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(fileURL);
        } catch (error) {
            console.error("Failed to download audio: ", error);
        }
    };

    return (
        <Layout>
            <Container sx={{ mt: 4, maxWidth: 'xl' }}>
                <Paper sx={{ p: 2, width: 'auto', overflowX: 'auto' }}>
                    <Grid container direction={"row"} sx={{ mb: 2}}>
                        <Typography component="h2" variant="h6" color="primary" gutterBottom sx={{ flexGrow: 1 }}>
                            Archiwum zaliczeń
                        </Typography>
                        <Button variant={"contained"} disabled={rowSelection.length === 0} onClick={handleReportButtonClick}>
                            Wygeneruj i pobierz raport
                        </Button>
                    </Grid>
                    <MaterialReactTable table={reportTable}/>
                </Paper>
            </Container>
        </Layout>
    );
};

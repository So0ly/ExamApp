import React, { useEffect, useMemo, useState } from 'react';
import {
    Container,
    IconButton,
    Paper,
    Typography,
    Button, Grid
} from '@mui/material';
import { Delete } from '@mui/icons-material/';
import { reportApi } from './api';
import { Layout } from '../layout';
import { examinerApi } from "../examiners";
import { format } from "date-fns"
import { MaterialReactTable, useMaterialReactTable } from "material-react-table";

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
                {accessorKey: 'examDuration', header: 'Czas trwania'},
                {
                    id: 'audioButton',
                    Header: 'Audio',
                    Cell: ({ row }) => {
                        return row.original.audioURL
                            ? <Button variant={"contained"} onClick={() => handleAudioButtonClick(row.original.audioURL)}>Pobierz audio</Button>
                            : <Typography align={"center"}>Brak nagrania</Typography>;
                    },
                },
            ];
            if(isAdmin){
                baseColumns.push({accessorKey: 'examinerName', header: 'Egzaminator'},
                    {
                        id: 'deleteButton',
                        Header: 'Usuń',
                        Cell: ({ row }) => {
                            return <IconButton onClick={() => deleteReport(row.original)}><Delete /></IconButton>;
                        }
                    },)
            }
            return baseColumns;
        }, [isAdmin]
    );

    const currentData = isAdmin ? allReports ?? [] : reports ?? [];

    useEffect(() => {
    }, [currentData]);

    useEffect(() => {
    }, [rowSelection]);

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
        grouping: ['className', 'examDate'],
        sorting:  [{ id: 'className', desc: true }, { id: 'examDate', desc: true }],
        pagination: { pageIndex: 0, pageSize: 100 },
    }), []);

    const DetailPanel = ({ row }) => {
        const questions = row.original.reportQuestions;

        return (
            <div>
                {questions.map((q, index) => (
                    <div key={index}>
                        <strong>Pytanie:</strong> {q.question} - <strong>Ocena:</strong> {q.grade}
                    </div>
                ))}
            </div>
        );
    };


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
        onRowSelectionChange: setRowSelection,
        state: {rowSelection},
        renderDetailPanel: ({ row }) => <DetailPanel row={row} />,
    });

    const downloadFile = (blob, filename) => {
        const fileURL = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = fileURL;
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(fileURL);
    };

    const handleReportButtonClick = async () => {
        try {
            const numericIds = Object.keys(rowSelection).filter(key => !isNaN(+key) && rowSelection[key]);
            const response = await generateReport({ ids: numericIds }).unwrap();
            const fileResponse = await getPDF(response.url);
            downloadFile(fileResponse.data, response.url);
        } catch (error) {
            console.error("Failed to generate report: ", error);
        }
    };

    const handleAudioButtonClick = async (fileName) => {
        try {
            const fileResponse = await getAudio(fileName);
            downloadFile(fileResponse.data, fileName);
        } catch (error) {
            console.error("Failed to download audio: ", error);
        }
    };

    return (
        <Layout>
            <Container sx={{ mt: 4 }}>
                <Paper sx={{ p: 2 }}>
                    <Grid container direction={"row"} sx={{ mb: 2}}>
                        <Typography component="h2" variant="h6" color="primary" gutterBottom sx={{ flexGrow: 1 }}>
                            Archiwum zaliczeń
                        </Typography>
                        <Button variant={"contained"} disabled={rowSelection.length === 0} onClick={handleReportButtonClick}>
                            Wygeneruj i pobierz raport
                        </Button>
                    </Grid>
                    <MaterialReactTable table={reportTable} />
                </Paper>
            </Container>
        </Layout>
    );
};

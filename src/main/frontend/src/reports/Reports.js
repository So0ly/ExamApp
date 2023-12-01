import React from 'react';
import {Container, IconButton, Paper, Table, TablePagination, TableBody, TableCell, TableHead, TableRow, Typography} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import {api} from './api';
import {Layout} from '../layout';
import {HasRole} from '../auth';

export const Reports = () => {
    const {data: allReports} = api.endpoints.getAllReports.useQuery(undefined, {pollingInterval: 10000});
    const {data: reports} = api.endpoints.getReports.useQuery(undefined, {pollingInterval: 10000});
    const [deleteReport] = api.endpoints.deleteReport.useMutation();

    return <Layout>
        <Container sx={{mt: theme => theme.spacing(2)}}>
            <Paper sx={{p: 2}}>
                <Typography component="h2" variant="h6" color="primary" gutterBottom>
                    Archiwum zaliczeń
                </Typography>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Nazwa przedmiotu</TableCell>
                            <TableCell>Data zaliczenia</TableCell>
                            <TableCell>Student</TableCell>
                            <TableCell>Egzaminator</TableCell>
                            <TableCell>Ocena końcowa</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <HasRole role='admin'>
                            {allReports && allReports.map(report =>
                                <TableRow key={report.id}>
                                    <TableCell>{report.className}</TableCell>
                                    <TableCell>{report.examDate}</TableCell>
                                    <TableCell>{report.student.firstName} {report.student.lastName}</TableCell>
                                    <TableCell>{report.examiner.titles} {report.examiner.firstName} {report.examiner.lastName}</TableCell>
                                    <TableCell>{report.finalGrade}</TableCell>
                                    <TableCell align='right'>
                                        <IconButton
                                            onClick={() => deleteReport(report)}
                                        >
                                            <DeleteIcon/>
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            )}
                        </HasRole>
                        {reports && reports.map(report =>
                            <TableRow key={report.id}>
                                <TableCell>{report.className}</TableCell>
                                <TableCell>{report.examDate}</TableCell>
                                <TableCell>{report.student.firstName} {report.student.lastName}</TableCell>
                                <TableCell>{report.examiner.titles} {report.examiner.firstName} {report.examiner.lastName}</TableCell>
                                <TableCell>{report.finalGrade}</TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                    <TablePagination rowsPerPageOptions={[1, 50, { value: -1, label: 'All' }]} />
                </Table>
            </Paper>
        </Container>
    </Layout>;
};
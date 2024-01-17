import React, { useState } from 'react';
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

export const Reports = () => {
    const {data: userData} = examinerApi.endpoints.getSelf.useQuery();
    const {data: allReports} = reportApi.endpoints.getAllReports.useQuery(undefined);
    const {data: reports} = reportApi.endpoints.getReports.useQuery(undefined);
    const [deleteReport] = reportApi.endpoints.deleteReport.useMutation();
    const [generateReport] = reportApi.endpoints.generateReport.useMutation();
    const [getPDF] = reportApi.endpoints.getPDF.useLazyQuery();
    const [getAudio] = reportApi.endpoints.getAudio.useLazyQuery();

    const [order, setOrder] = useState('desc');
    const [orderBy, setOrderBy] = useState('examDate');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(5);
    const [selected, setSelected] = useState([]);
    const [expandedRows, setExpandedRows] = useState(new Set());

    const columns = [
        { id: 'className', label: 'Nazwa przedmiotu', align: 'left' },
        { id: 'examDate', label: 'Data zaliczenia', align: 'left' },
        { id: 'studentName', label: 'Student', align: 'left' },
        { id: 'examinerName', label: 'Egzaminator', align: 'left' },
        { id: 'finalGrade', label: 'Ocena końcowa', align: 'left' },
        { id: 'examDuration', label: 'Czas trwania', align: 'left' }
    ];

    const handleRequestSort = (property) => (event) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleAllCheckBoxes = (event) => {
        if (event.target.checked) {
            const newSelected = paginatedReports.map((n) => n.id);
            setSelected(newSelected);
            return;
        }
        setSelected([]);
    };

    const handleSingleCheckBox = (event, id) => {
        const selectedIndex = selected.indexOf(id);
        let newSelected = [];

        if (selectedIndex === -1) {
            newSelected = newSelected.concat(selected, id);
        } else if (selectedIndex === 0) {
            newSelected = newSelected.concat(selected.slice(1));
        } else if (selectedIndex === selected.length - 1) {
            newSelected = newSelected.concat(selected.slice(0, -1));
        } else if (selectedIndex > 0) {
            newSelected = newSelected.concat(
                selected.slice(0, selectedIndex),
                selected.slice(selectedIndex + 1),
            );
        }

        setSelected(newSelected);
    };

    const handleReportButtonClick = async () => {
        try {
            const response = await generateReport({ ids: selected }).unwrap();
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
            console.error("Failed to generate report: ", error);
        }
    };

    //TODO: a table of questions inside a row
    const handleRowExpansion = (reportId) => {
        const newExpandedRows = new Set(expandedRows);
        if (newExpandedRows.has(reportId)) {
            newExpandedRows.delete(reportId);
        } else {
            newExpandedRows.add(reportId);
        }
        setExpandedRows(newExpandedRows);
    };

    const isSelected = (id) => selected.indexOf(id) !== -1;

    const isAdmin = userData && userData.roles.includes('admin');

    const currentData = isAdmin ? allReports : reports;

    const sortedReports = currentData ? currentData.slice().sort((a, b) => {
        let compare = 0;

        switch (orderBy) {
            case 'examDate':
                compare = new Date(a.examDate) - new Date(b.examDate);
                break;
            case 'className':
                compare = a.className.localeCompare(b.className);
                break;
            case 'studentName':
                compare = a.student.lastName.localeCompare(b.student.lastName);
                break;
            case 'examinerName':
                compare = a.examiner.lastName.localeCompare(b.examiner.lastName);
                break;
            case 'finalGrade':
                compare = parseFloat(a.finalGrade) - parseFloat(b.finalGrade);
                break;
            case 'examDuration':
                compare = a.examiner.examDuration.localeCompare(b.examiner.examDuration);
                break;
            default:
                compare = new Date(a.examDate) - new Date(b.examDate);
                break;
        }

        return order === 'asc' ? compare : -compare;
    }) : [];


    const paginatedReports = sortedReports.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

    return (
        <Layout>
            <Container sx={{ mt: 4 }}>
                <Paper sx={{ p: 2 }}>
                    <Grid container direction={"row"}>
                        <Typography component="h2" variant="h6" color="primary" gutterBottom sx={{ flexGrow: 1 }}>
                            Archiwum zaliczeń
                        </Typography>
                        <Button variant={"contained"} disabled={selected.length === 0} onClick={handleReportButtonClick}>
                            Wygeneruj i pobierz raport
                        </Button>
                    </Grid>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>
                                    <Checkbox
                                        indeterminate={selected.length > 0 && selected.length < paginatedReports.length}
                                        checked={paginatedReports.length > 0 && selected.length === paginatedReports.length}
                                        onChange={handleAllCheckBoxes}
                                    />
                                </TableCell>
                                <TableCell>
                                    <TableSortLabel
                                        active={orderBy === 'className'}
                                        direction={orderBy === 'className' ? order : 'asc'}
                                        onClick={handleRequestSort('className')}
                                    >
                                        Nazwa przedmiotu
                                    </TableSortLabel>
                                </TableCell>
                                <TableCell>
                                    <TableSortLabel
                                        active={orderBy === 'examDate'}
                                        direction={orderBy === 'examDate' ? order : 'asc'}
                                        onClick={handleRequestSort('examDate')}
                                    >
                                        Data zaliczenia
                                    </TableSortLabel>
                                </TableCell>
                                <TableCell>
                                    <TableSortLabel
                                        active={orderBy === 'studentName'}
                                        direction={orderBy === 'studentName' ? order : 'asc'}
                                        onClick={handleRequestSort('studentName')}
                                    >
                                        Student
                                    </TableSortLabel>
                                </TableCell>
                                {isAdmin && (
                                    <TableCell>
                                    <TableSortLabel
                                        active={orderBy === 'examinerName'}
                                        direction={orderBy === 'examinerName' ? order : 'asc'}
                                        onClick={handleRequestSort('examinerName')}
                                    >
                                        Egzaminator
                                    </TableSortLabel>
                                </TableCell>
                                    )}
                                <TableCell>
                                    <TableSortLabel
                                        active={orderBy === 'finalGrade'}
                                        direction={orderBy === 'finalGrade' ? order : 'asc'}
                                        onClick={handleRequestSort('finalGrade')}
                                    >
                                        Ocena końcowa
                                    </TableSortLabel>
                                </TableCell>
                                <TableCell>
                                    <TableSortLabel
                                        active={orderBy === 'examDuration'}
                                        direction={orderBy === 'examDuration' ? order : 'asc'}
                                        onClick={handleRequestSort('examDuration')}
                                    >
                                        Czas trwania
                                    </TableSortLabel>
                                </TableCell>
                                {isAdmin && (
                                    <TableCell>Akcje</TableCell>
                                )}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {paginatedReports.map((report) => {
                                const isItemSelected = isSelected(report.id);
                                const isRowExpanded = expandedRows.has(report.id);

                                return(
                                    <TableRow key={report.id}
                                    hover
                                    onClick={(event) => handleSingleCheckBox(event, report.id)}
                                    role="checkbox"
                                    aria-checked={isItemSelected}
                                    selected={isItemSelected}
                                    >
                            <TableCell padding="checkbox">
                                <Checkbox
                                    checked={isItemSelected}
                                />
                            </TableCell>
                                    <TableCell>{report.className}</TableCell>
                                    <TableCell>{report.examDate}</TableCell>
                                    <TableCell>{`${report.student.firstName} ${report.student.lastName}`}</TableCell>
                                        {isAdmin && (
                                    <TableCell>{`${report.examiner.titles} ${report.examiner.firstName} ${report.examiner.lastName}`}</TableCell>
                                        )}
                                    <TableCell>{report.finalGrade}</TableCell>
                                    <TableCell>{report.examDuration}</TableCell>
                                        {isAdmin && (
                                            <TableCell align='right'>
                                                    {/*<IconButton onClick={() => handleRowExpansion(report.id)}>*/}
                                                    {/*    {isRowExpanded ? <ExpandLess /> : <ExpandMore />}*/}
                                                    {/*</IconButton>*/}
                                                    <IconButton onClick={() => deleteReport(report)}>
                                                        <Delete />
                                                    </IconButton>
                                            </TableCell>
                                            )}

                                </TableRow>
                        );
                        })}
                        </TableBody>
                    </Table>
                    <TablePagination
                        rowsPerPageOptions={[5, 10, 25]}
                        component="div"
                        count={(currentData?.length || 0)}
                        rowsPerPage={rowsPerPage}
                        page={page}
                        onPageChange={handleChangePage}
                        onRowsPerPageChange={handleChangeRowsPerPage}
                    />
                </Paper>
            </Container>
        </Layout>
    );
};

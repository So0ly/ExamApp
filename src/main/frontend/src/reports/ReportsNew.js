import { Layout } from "../layout";
import React, {useRef, useState} from "react";
import { useDispatch, useSelector } from 'react-redux';
import {
    Container,
    Typography,
    Grid,
    Table,
    TableCell,
    TableRow,
    TableHead,
    TableBody,
    TextField, Button, Modal, Box
} from '@mui/material';
import {GridCard} from "./GridCard";
import {reportApi} from "./api";
import {studentApi} from "../students"
import {FormCard} from "./FormCard";
import {useNavigate} from "react-router-dom";
import {
    setQuestionList,
    setSelectedQuestionList,
    setStudentList,
    setCurrentExam,
    clearData,
    setCurrentStudent,
    setQuestionTime
} from "../exam/redux";
import {PopUp} from "./PopUp";

export const ReportsNew = () => {
    const studentInput = useRef(null);
    const questionInput = useRef(null);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const { studentList, uploadedQuestionList, selectedQuestionList, currentStudent, currentExam, questionTime } = useSelector(state => state.exam);
    const [showErrorPopUp, setShowErrorPopUp] = useState(false);
    const [showRandomPopUp, setShowRandomPopUp] = useState(false);
    const [errorPopUpMsg, setErrorPopUpMsg] = useState('');
    const [questionCount, setQuestionCount] = useState('');

    const [studentCSV] = studentApi.endpoints.studentCSV.useMutation();
    const [questionCSV] = reportApi.endpoints.questionCSV.useMutation();

    const handleAppendStudentList = (formData) => {
        const arrayified = Array.isArray(formData) ? formData : [formData];
        dispatch(setCurrentStudent(arrayified[0]));
        dispatch(setStudentList([...studentList, ...arrayified]));
    };

    const handleAppendQuestionList = (formData) => {
        const arrayified = Array.isArray(formData) ? formData : [formData];
        const questions = arrayified.map(item => item.question);
        dispatch(setQuestionList([...uploadedQuestionList, ...questions]));
        dispatch(setSelectedQuestionList([...selectedQuestionList, ...questions]));
    };

    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const formData = new FormData();
        formData.append('file', file);
        if (e.target === studentInput.current) {
            const response = await studentCSV(formData).unwrap();
            handleAppendStudentList(response);
        } else if (e.target === questionInput.current) {
            const response = await questionCSV(formData).unwrap();
            handleAppendQuestionList(response);
        }
    };

    const handleUpload = (fileInput) => {
        fileInput.current.click();
    };

    const handleTitleChange = (event) => {
        dispatch(setCurrentExam(event.target.value));
    };

    const handleClearData = () => {
        dispatch(clearData())
    };

    const validateStart = () => {
        if(currentExam === ''){
            setErrorPopUpMsg('Brak tytułu egzaminu');
            setShowErrorPopUp(true);
            return false;
        } else if(currentStudent === null) {
            setErrorPopUpMsg('Brak wybranego studenta');
            setShowErrorPopUp(true);
            return false;
        } else if(selectedQuestionList.length === 0) {
            setErrorPopUpMsg('Brak wybranych pytań');
            setShowErrorPopUp(true);
            return false;
        } else {
            setShowErrorPopUp(false);
            return true;
        }
    };

    const handleStartExam = () => {
        if (validateStart()){
            navigate('/exam');
        }
    };

    const handleSelectStudent = (student) => {
        if (currentStudent && student.index === currentStudent.index) {
            dispatch(setCurrentStudent(null));
        } else {
            dispatch(setCurrentStudent(student));
        }
    };

    const handleSelectQuestion = (question) => {
        if (selectedQuestionList.includes(question)){
            const newSelected = selectedQuestionList.filter(listEntry => listEntry !== question);
            dispatch(setSelectedQuestionList(newSelected));
        } else {
            dispatch(setSelectedQuestionList([...selectedQuestionList, question]));
        }
    };

    function getRandomQuestions(questionCount) {
        let listCopy = [...uploadedQuestionList];
        if (questionCount >= listCopy.length){
            return listCopy;
        }
        let randomQuestions = [];

        for (let i = 0; i < questionCount; i++) {
            if (listCopy.length === 0) {
                break;
            }
            let random = Math.floor(Math.random() * listCopy.length);
            randomQuestions.push(listCopy[random]);
            listCopy.splice(random, 1);
        }
        return randomQuestions;
    }

    const handleRandomPopUpOpen = () => {
        setShowRandomPopUp(true);
    };

    const handleAcceptRandom = () => {
        dispatch(setSelectedQuestionList(getRandomQuestions(questionCount)));
        setShowRandomPopUp(false);
    }

    const handleChooseRandom = (event) => {
        setQuestionCount(event.target.value);
    }

    const handleClosePopUp = (setPopUpState) => {
        setPopUpState(false);
    };

    const handleQuestionTimer = (event) => {
        let time = parseInt(event.target.value);
        if (time < 40) {
            time = 40
        }
        dispatch(setQuestionTime(time));
    }

    return (
        <Layout>
            <PopUp title={"Błąd"} popUpMsg={errorPopUpMsg} open={showErrorPopUp} close={() => handleClosePopUp(setShowErrorPopUp)}/>
            <PopUp title={"Wylosuj pytania"} popUpMsg={"Wylosuj daną ilość pytań:"} open={showRandomPopUp} close={() => handleClosePopUp(setShowRandomPopUp)}>
                <TextField placeholder={"Podaj liczbę"} onChange={handleChooseRandom}/>
                <Button variant={"contained"} onClick={handleAcceptRandom} sx={{ mt: 2 }}>Akceptuj</Button>
            </PopUp>
            <Container sx={{ mt: 2 }}>
                <TextField placeholder={"Nazwa przedmiotu"} fullWidth required={true} sx={{ mt: 2 }} onChange={handleTitleChange} value={currentExam || ''}/>
                <Typography component="h2" variant="h6" color="primary" gutterBottom align="center" margin={"normal"}>
                    Wczytaj studentów
                </Typography>
                <Grid container spacing={2} justifyContent="space-evenly">
                    <GridCard onClick={() => handleUpload(studentInput)}/>
                    <input
                        type="file"
                        accept=".csv"
                        onChange={handleFileChange}
                        style={{ display: 'none' }}
                        ref={studentInput}
                    />
                    <FormCard
                        fields={[
                            { name: 'firstName', label: 'Imię', required: true },
                            { name: 'lastName', label: 'Nazwisko', required: true },
                            { name: 'index', label: 'Index', required: true, type:'number' }
                        ]}
                        onSubmit={(formData) => handleAppendStudentList(formData)}
                    />
                </Grid>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Imię</TableCell>
                            <TableCell>Nazwisko</TableCell>
                            <TableCell>Nr indeksu</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                    {studentList.map((student => (
                            <TableRow key={student.index} onClick={() => handleSelectStudent(student)}
                        selected={currentStudent && student.index === currentStudent.index}
                        style={{ cursor: 'pointer'}}
                        >
                                <TableCell>{student.firstName}</TableCell>
                                <TableCell>{student.lastName}</TableCell>
                                <TableCell>{student.index}</TableCell>
                            </TableRow>
                        )
                    ))}
                    </TableBody>
                </Table>
            </Container>
            <Container sx={{ mt: 2 }}>
                <Typography component="h2" variant="h6" color="primary" gutterBottom align="center">
                    Wczytaj pytania
                </Typography>
                <Grid container spacing={2} justifyContent="space-evenly">
                    <GridCard onClick={() => handleUpload(questionInput)}/>
                    <input
                        type="file"
                        accept=".csv"
                        onChange={handleFileChange}
                        style={{ display: 'none' }}
                        ref={questionInput}
                        id={"aaa"}
                    />
                    <FormCard fields={[
                        { name: 'question', label: 'Pytanie', required: true },
                    ]}
                              onSubmit={(formData) => handleAppendQuestionList(formData)} />
                </Grid>
                <Grid container spacing={2} justifyContent={"space-evenly"} sx={{mt: 2}}>
                    <Typography variant={"h6"} color="primary">
                        Podaj ilość czasu na pytanie (min. 40s):
                    </Typography>
                    <TextField placeholder={"Minimum 40s"} type={"number"} onChange={handleQuestionTimer} value={questionTime || ''}/>
                </Grid>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Pytania</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                {uploadedQuestionList.map((question => (
                    <TableRow key={question} onClick={() => handleSelectQuestion(question)}
                              selected={selectedQuestionList.includes(question)}
                              style={{ cursor: 'pointer'}}>
                        <TableCell>{question}</TableCell>
                    </TableRow>
                    )
                ))}
                    </TableBody>
                </Table>
                <Grid container spacing={2} justifyContent="space-evenly" sx={{ mb: 2, mt: 2 }}>
                    <Grid item>
                        <Button onClick={handleClearData} variant={"outlined"}>
                            Wyczyść dane
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button onClick={handleRandomPopUpOpen} variant={"contained"}>
                            Wylosuj pytania
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button onClick={handleStartExam} variant={"contained"}>
                            Przejdź do egzaminu
                        </Button>
                    </Grid>
                </Grid>
            </Container>
        </Layout>
    );
};

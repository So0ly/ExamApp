import {Layout} from "../layout";
import {useDispatch, useSelector} from "react-redux";
import {Box, Button, Checkbox, Container, Grid, Typography} from "@mui/material";
import {useEffect, useState} from "react";
import {KeyboardDoubleArrowLeft, KeyboardDoubleArrowRight} from '@mui/icons-material';
import {finishExam} from "./redux";
import {reportApi} from "../reports";
import {useNavigate} from "react-router-dom";
import {CountdownCircleTimer} from "react-countdown-circle-timer";
import {GradeRadio} from "./GradeRadio";

export const Exam = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const {currentExam, currentStudent, selectedQuestionList, questionTime} = useSelector(state => state.exam);
    const [currentQuestionPtr, setCurrentQuestionPtr] = useState(0);
    const [isExam, setIsExam] = useState(false);
    const [startTime, setStartTime] = useState(null);
    const [isPermission, setIsPermission] = useState(false);
    const [isRecording, setIsRecording] = useState(false);
    const [audioData, setAudioData] = useState(null);
    const [mediaRecorder, setMediaRecorder] = useState(null);
    const [addReport] = reportApi.endpoints.addReport.useMutation();

    const [selectedGrade, setSelectedGrade] = useState("5");
    const [questionsGrades, setQuestionsGrades] = useState({});

    useEffect(() => {
        if (questionsGrades[selectedQuestionList[currentQuestionPtr]] !== undefined) {
            setSelectedGrade(questionsGrades[selectedQuestionList[currentQuestionPtr]]);
        } else {
            setSelectedGrade("5");
        }
    }, [currentQuestionPtr, questionsGrades, selectedQuestionList]);

    useEffect(() => {
        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
            navigator.mediaDevices.getUserMedia({ audio: true })
                .then(stream => {
                    console.log("Microphone access granted");
                }).catch(error => {
                console.error("Microphone access denied:", error);
            });
        }
    }, []);

    const handleNextQuestion = () => {
        if (currentQuestionPtr < selectedQuestionList.length - 1) {
            setQuestionsGrades(prevGrades => ({
                ...prevGrades,
                [selectedQuestionList[currentQuestionPtr]]: selectedGrade
            }));
            setCurrentQuestionPtr(currentQuestionPtr + 1);
        }
    };

    const handlePreviousQuestion = () => {
        if (currentQuestionPtr > 0) {
            setQuestionsGrades(prevGrades => ({
                ...prevGrades,
                [selectedQuestionList[currentQuestionPtr]]: selectedGrade
            }));
            setCurrentQuestionPtr(currentQuestionPtr - 1);
        }
    };

    const handleStart = () => {
        if(isPermission){
            startRecording();
        }
        setIsExam(true);
        setStartTime(Date.now());
    };

    const handlePermission = () => {
        setIsPermission(!isPermission);
    };

    const startRecording = async () => {
        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
            try {
                const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                const mediaRecorder = new MediaRecorder(stream);
                const audioChunks = [];

                mediaRecorder.ondataavailable = event => {
                    audioChunks.push(event.data);
                };

                mediaRecorder.onstop = () => {
                    const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
                    setAudioData(audioBlob);
                    console.log("BBB")
                };

                mediaRecorder.start();
                setIsRecording(true);

                setMediaRecorder(mediaRecorder);
            } catch (error) {
                console.error('Error starting audio recording:', error);
            }
        }
    };

    const handleFinishExam = async () => {
        setQuestionsGrades(prevGrades => ({
            ...prevGrades,
            [selectedQuestionList[currentQuestionPtr]]: selectedGrade
        }));
        let updatedGrades = {
            ...questionsGrades,
            [selectedQuestionList[currentQuestionPtr]]: selectedGrade
        };
        let finishTime = Math.floor((Date.now() - startTime) / 1000);
        let min = Math.floor(finishTime / 60);
        let sec = finishTime % 60;
        let finishStr = `${min.toString()}m ${sec.toString()}s`
        if (isRecording && mediaRecorder && mediaRecorder.state !== 'inactive') {
            mediaRecorder.stop();
            setIsRecording(false);
        }
        let report = {
            student: {
                "firstName": currentStudent.firstName,
                "lastName": currentStudent.lastName,
                "index": currentStudent.index,
            },
            className: currentExam,
            "questions": updatedGrades,
            examDuration: finishStr
        };
        await addReport(report);
        dispatch(finishExam());
        navigate('/reports/new');
    };

    const handleGradeChange = (event) => {
        setSelectedGrade(event.target.value);
    };

    return (
        <Layout>
            {!isExam && (
                <Container sx={{ mt: 2 }}>
                    <Grid container spacing={2} justifyContent="space-evenly" direction={"column"} alignItems="center">
                        <Grid item><Typography variant={"h1"} color={"primary"}>Egzamin z przedmiotu:</Typography></Grid>
                        <Grid item><Typography variant={"h1"} color={"primary"}>{currentExam}</Typography></Grid>
                        <Grid item><Typography variant={"h3"}>Egzaminowany: {currentStudent.firstName} {currentStudent.lastName} - {currentStudent.index}</Typography></Grid>
                        <Grid item><Typography variant={"h3"}>Ilość pytań: {selectedQuestionList.length}</Typography></Grid>
                        <Grid item><Typography variant={"h3"}>Ilość czasu na pytanie: {questionTime}</Typography></Grid>
                        <Grid item><Typography variant={"h3"}>Powodzenia!</Typography></Grid>
                        <Grid item><Button variant={"contained"} onClick={handleStart}>Rozpocznij egzamin</Button></Grid>
                        <Grid item>
                            <Button variant={"contained"} onClick={handlePermission}>
                                Nagrywać audio? <Checkbox checked={isPermission} color={"default"} />
                            </Button>
                        </Grid>
                    </Grid>
                </Container>
            )}
        {isExam && (
            <Container sx={{
                mt: 2,
                height: '90vh',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
            }}>
                <Typography variant="h6" align="center">
                    Pytanie {currentQuestionPtr + 1}/{selectedQuestionList.length}
                </Typography>

                <Typography variant="h1" align="center">
                    {selectedQuestionList[currentQuestionPtr]}
                </Typography>
                <GradeRadio value={selectedGrade} onChange={handleGradeChange} />
                <Grid container spacing={2} justifyContent="center" sx={{ width: '100%' }}>
                    <Grid item xs={6} display="flex" justifyContent="flex-end">
                        <Button onClick={handlePreviousQuestion} disabled={currentQuestionPtr === 0} variant="contained">
                            <KeyboardDoubleArrowLeft />
                        </Button>
                    </Grid>
                    <Grid item xs={6} display="flex" justifyContent="flex-start">
                        <Button onClick={handleNextQuestion} disabled={currentQuestionPtr === selectedQuestionList.length - 1} variant="contained">
                            <KeyboardDoubleArrowRight />
                        </Button>
                    </Grid>
                </Grid>

                {currentQuestionPtr === selectedQuestionList.length - 1 && (
                    <Button variant="contained" onClick={handleFinishExam} sx={{ mt: 2 }}>
                        Zakończ
                    </Button>
                )}

                <Box sx={{ position: 'relative', left: 0, bottom: -10 }}>
                    <CountdownCircleTimer
                        key={currentQuestionPtr}
                        isPlaying ={true}
                        duration={parseInt(questionTime)}
                        colors={['#004777', '#F7B801', '#A30000', '#A30000']}
                        colorsTime={[7, 5, 2, 0]}
                        size={80}
                        strokeWidth={6}
                    >
                        {({ remainingTime }) => <Typography variant="h4">{remainingTime}</Typography>}
                    </CountdownCircleTimer>
                </Box>
            </Container>
        )}
        </Layout>
    );
};
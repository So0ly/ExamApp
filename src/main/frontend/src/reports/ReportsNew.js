import {Layout} from "../layout";
import {api} from './api'
import {Container, Paper, Typography, Card, Box, Button, IconButton, Grid} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import React, {useState} from "react";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from '@mui/icons-material/Edit';

export const ReportsNew = () => {
    const [selectedFiles, setSelectedFiles] = useState([]);

    const handleDragOver = (e) => {
        e.preventDefault();
    };

    const handleDrop = (e) => {
        e.preventDefault();

        const files = Array.from(e.dataTransfer.files);
        setSelectedFiles((prevFiles) => [...prevFiles, ...files]);
    };

    const handleFileChange = (e) => {
        const files = Array.from(e.target.files);
        setSelectedFiles((prevFiles) => [...prevFiles, ...files]);
    };

    const handleUploadClick = () => {

        console.log('Uploading files:', selectedFiles);
    };

    const removeFile = (fileName) => {
        setSelectedFiles((prevFiles) =>
            prevFiles.filter((file) => file.name !== fileName)
        );
    };

    return <Layout>
        <Container sx={{mt: theme => theme.spacing(2)}}>
                <Typography component="h2" variant="h6" color="primary" gutterBottom align="center">
                    Wczytaj studentów
                </Typography>
            <Paper>
                <Grid container
                    justifyContent = "space-evenly"
                >
                    <Grid item>
                <Card

                    sx={{ textAlign: 'center' }}
                    onDragOver={handleDragOver}
                    onDrop={handleDrop}
                >
                    <CloudUploadIcon fontSize="large" />
                    <Typography variant="body1" gutterBottom>
                        Importuj z CSV
                    </Typography>
                    <input
                        type="file"
                        accept=".csv"
                        onChange={handleFileChange}
                        style={{ display: 'none' }}
                        id="fileInput"
                    />
                    <label htmlFor="fileInput">
                        <Button variant="outlined" color="primary" component="span">
                            Wybierz plik
                        </Button>
                    </label>
                    {selectedFiles.length > 0 && (
                        <Box mt={2}>
                            <Typography variant="body2" color="textSecondary">
                                Wybrane pliki:
                            </Typography>
                            <ul>
                                {selectedFiles.map((file, index) => (
                                    <li key={index}>{file.name}<IconButton
                                        onClick={() => removeFile(file.name)}
                                    >
                                        <DeleteIcon/>
                                    </IconButton></li>
                                ))}
                            </ul>
                            <Button variant="contained" color="primary" onClick={handleUploadClick}>
                                Upload
                            </Button>
                        </Box>
                    )}
                </Card>
                    </Grid>
                <Card
                    sx={{ textAlign: 'center' }}
                    // onClick={} TODO Pop-up do wpisywania
                >
                    <EditIcon fontSize='large'/>
                    <Typography>Wpisz ręcznie</Typography>

                </Card>
            </Grid>
            </Paper>
        </Container>
    </Layout>
};
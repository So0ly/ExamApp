import React, { useState, forwardRef } from 'react';
import { Card, Typography, Grid, TextField, Button, Box } from '@mui/material';
import EditIcon from "@mui/icons-material/Edit";

export const FormCard = forwardRef(({ fields, onSubmit }, ref) => {
    const [formData, setFormData] = useState(
        fields.reduce((acc, field) => ({ ...acc, [field.name]: '' }), {})
    );
    const [validationErrorList, setValidationErrorList] = useState({});

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData(prevState => ({ ...prevState, [name]: value }));
    };

    const validateForm = () => {
        const newErrors = {};
        fields.forEach(field => {
            if (field.required && !formData[field.name].trim()) {
                newErrors[field.name] = 'Pole jest wymagane';
            }
        });
        setValidationErrorList(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = () => {
        if (validateForm()) {
            onSubmit(formData);
            setFormData(fields.reduce((acc, field) => ({ ...acc, [field.name]: '' }), {}));
            setValidationErrorList({});
        }
    };


    return (
        <Grid item xs={12} sm={6}>
            <Card sx={{
                p: 2,
                height: '100%'
            }} align="center">
                <EditIcon fontSize="large" />
                <Typography variant="body1" gutterBottom>
                    Wpisz ręcznie
                </Typography>
                <Box noValidate sx={{ mt: 1 }}>
                    {fields.map((field, index) => (
                        <TextField
                            key={index}
                            margin="normal"
                            required={field.required || false}
                            fullWidth
                            autoFocus={index === 0}
                            label={field.label}
                            name={field.name}
                            type={field.type || 'text'}
                            onChange={handleChange}
                            value={formData[field.name]}
                            error={!!validationErrorList[field.name]}
                            helperText={validationErrorList[field.name] || ''}
                        />
                    ))}
                    <Button
                        fullWidth
                        variant="contained"
                        onClick={handleSubmit}
                        sx={{ mt: 3, mb: 2 }}
                    >
                        Dodaj
                    </Button>
                </Box>
            </Card>
        </Grid>
    );
});

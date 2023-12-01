import {Avatar, Box, Button, Container, Snackbar, TextField, Typography} from "@mui/material";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import {useForm} from '../useForm';
import React from "react";
import {useDispatch} from "react-redux";
import {useNavigate} from "react-router-dom";
import {login} from "./redux";

export const Register = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const {values, isValid, error, setError, onChange} = useForm({
        initialValues: {mail: '', password: '', firstName: '', lastName: '', titles: ''}
    });
    const sendLogin = () => {
        if (isValid) {
            dispatch(login({mail: values.mail, password: values.password}))
                .then(({meta, payload}) => {
                    if (meta.requestStatus === 'fulfilled') {
                        navigate('/');
                    } else if (payload?.status === 401) {
                        setError('Invalid credentials');
                    } else {
                        setError('Error');
                    }
                });
        }
    };

    return (
        <Container maxWidth='xs'>
            <Box sx={{mt: theme => theme.spacing(8), display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Avatar sx={{m: 1}}>
                    <LockOutlinedIcon />
                </Avatar>
                <Typography component='h1' variant='h5'>
                    Zaloguj się do apliakcji
                </Typography>
                <Box noValidate sx={{ mt: 1 }}>
                    <TextField margin='normal' required fullWidth autoFocus
                               label='Mail' name='mail' onChange={onChange} value={values.mail}
                    />
                    <TextField type='password' margin='normal' required fullWidth
                               label='Hasło' name='password' onChange={onChange} value={values.password}
                               onKeyDown={e => e.key === 'Enter' && sendLogin()}
                    />
                    <Button fullWidth variant='contained' onClick={sendLogin} sx={{ mt: 3, mb: 2 }}>
                        Zaloguj się
                    </Button>
                </Box>
            </Box>
            <Snackbar
                open={Boolean(error)} message={error}
                autoHideDuration={6000} onClose={() => setError(null)}
            />
        </Container>
    );
}
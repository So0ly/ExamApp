import {
    Avatar,
    Box,
    Button,
    Container, FormControl,
    InputLabel,
    MenuItem,
    Select,
    Snackbar,
    TextField,
    Typography
} from "@mui/material";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import {useForm} from '../useForm';
import React from "react";
import {useDispatch} from "react-redux";
import {useNavigate} from "react-router-dom";
import {register} from "./redux";

export const Register = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const {values, setValue, isValid, error, setError, onChange} = useForm({
        initialValues: {mail: '', password: '', firstName: '', lastName: '', titles: ''}
    });

    const sendRegister = () => {
        if (isValid) {
            dispatch(register({mail: values.mail, password: values.password, firstName:  values.firstName, lastName: values.lastName, titles: values.titles}))
                .then(({meta, payload}) => {
                    if (meta.requestStatus === 'fulfilled') {
                        navigate('/login');
                    } else if (payload?.status === 401) {
                        setError('Konto o takim adresie email już istnieje');
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
                    Zarejestruj się w aplikacji
                </Typography>
                <Box noValidate sx={{ mt: 1 }}>
                    <TextField margin='normal' required fullWidth autoFocus helperText="Adres musi pochodzić z domeny pbs.edu.pl"
                               label='Mail' name='mail' onChange={onChange} value={values.mail}
                    />
                    <TextField type='password' margin='normal' required fullWidth
                               label='Hasło' name='password' onChange={onChange} value={values.password}
                    />
                    <TextField margin='normal' required fullWidth autoFocus
                               label='Imię' name='firstName' onChange={onChange} value={values.firstName}
                    />
                    <TextField margin='normal' required fullWidth autoFocus
                               label='Nazwisko' name='lastName' onChange={onChange} value={values.lastName}
                    />
                    <TextField margin='normal' required fullWidth autoFocus
                               label='Tytuł' name='titles' onChange={onChange} value={values.titles}
                    />
                    <Button fullWidth variant='contained' onClick={sendRegister} sx={{ mt: 3, mb: 2 }}>
                        Zarejestruj się
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
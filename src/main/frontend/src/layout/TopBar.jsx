import React from 'react';
import {AppBar, Button, IconButton, Toolbar, Tooltip, Typography} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';
import MenuIcon from '@mui/icons-material/Menu';
import {ExaminerIcon} from './UserIcon';

export const TopBar = ({goHome, reportsNew, toggleDrawer}) => (
    <AppBar
        position='fixed'
        sx={{
            zIndex: theme => theme.zIndex.drawer + 1
        }}
    >
        <Toolbar>
            <IconButton
                size='large'
                edge='start'
                color='inherit'
                aria-label='menu'
                onClick={toggleDrawer}
            >
                <MenuIcon />
            </IconButton>
            <Tooltip title='Home'>
                <IconButton
                    color='inherit'
                    onClick={goHome}
                >
                    <HomeOutlinedIcon />
                </IconButton>
            </Tooltip>
            <Typography variant='h6' component='div' sx={{ flexGrow: 1 }}>
                Aplikacja Egzaminacyjna
            </Typography>
            <Button
                variant="contained"
                color="success"
                onClick={reportsNew}
            >
                Nowy egzamin
                <AddIcon/>
            </Button>
            <ExaminerIcon/>
        </Toolbar>
    </AppBar>
);

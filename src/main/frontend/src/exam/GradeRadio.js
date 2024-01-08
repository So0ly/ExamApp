import React from 'react';
import { Radio, RadioGroup, FormControlLabel, FormControl } from '@mui/material';

export const GradeRadio = ({ value, onChange }) => {
    return (
        <FormControl component="fieldset">
            <RadioGroup row value={value} onChange={onChange}>
                <FormControlLabel value="2" control={<Radio />} label="2" />
                <FormControlLabel value="3" control={<Radio />} label="3" />
                <FormControlLabel value="3.5" control={<Radio />} label="3.5" />
                <FormControlLabel value="4" control={<Radio />} label="4" />
                <FormControlLabel value="4.5" control={<Radio />} label="4.5" />
                <FormControlLabel value="5" control={<Radio />} label="5" />
            </RadioGroup>
        </FormControl>
    );
};

import {examinerApi} from '../examiners';

export const HasRole = ({role, children}) => {
    const {data} = examinerApi.endpoints.getSelf.useQuery();
    if ((data?.roles ?? []).includes(role)) {
        return children;
    }
    return null;
}
/* eslint-disable no-param-reassign */
import * as types from './types';
import Http from '../utils/request';

export default {
  state: {
    domainInfo: {
      nameAndNamespace: '',
      sysAdmin: '',
      techAdmin: '',
    },
    jobInfo: {},
    userAuthority: {
      username: '',
      role: '',
      authority: [],
    },
  },

  getters: {
    domainInfo: state => state.domainInfo,
    jobInfo: state => state.jobInfo,
    userAuthority: state => state.userAuthority,
  },

  mutations: {
    [types.SET_DOMAIN_INFO](state, item) {
      state.domainInfo = {
        ...state.domainInfo,
        nameAndNamespace: item.nameAndNamespace,
        sysAdmin: item.sysAdmin,
        techAdmin: item.techAdmin,
      };
    },
    [types.SET_JOB_INFO](state, item) {
      state.jobInfo = {
        ...item,
      };
    },
    [types.SET_USER_AUTHORITY](state, item) {
      console.log(item);
      state.userAuthority = {
        ...state.userAuthority,
        username: item.username,
        role: item.role,
        authority: item.authority,
      };
      console.log(state.userAuthority);
    },
  },

  actions: {
    [types.SET_DOMAIN_INFO]({ commit }, domainInfo) {
      commit(types.SET_DOMAIN_INFO, domainInfo);
    },
    [types.SET_JOB_INFO]({ commit }, jobInfoParams) {
      return Http.get(`/console/namespaces/${jobInfoParams.domainName}/jobs/${jobInfoParams.jobName}/config`).then((data) => {
        commit(types.SET_JOB_INFO, data);
        return data;
      });
    },
    [types.SET_USER_AUTHORITY]({ commit }, userAuthority) {
      commit(types.SET_USER_AUTHORITY, userAuthority);
    },
  },
};

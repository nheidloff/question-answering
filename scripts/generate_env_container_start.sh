#!/bin/bash
cat <<EOF
#!/bin/bash
# start with container
export C_M_DIR_NAME=${SET_M_DIR_NAME}
export C_HOME_PATH=${HOME_PATH}
export C_SESSION_ID=${SET_SESSION_ID}
export C_CONT_CONF=${SET_CONT_CONF}
EOF
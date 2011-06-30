#!/bin/bash
#===================================================================================
#
#         FILE: start.sh
#
#        USAGE: Download bedework quickstart 3.7. Extract.
#               Copy paste into quickstart folder, launch in terminal ./start.sh
#               Follow the instructions.
#
# DESCRIPTION: Creates configuration for Bedework. Configures server.xml, bedework-ds.xml, 
#               cal.options.xml. Gives possiblity to configure port of jboss server,
#               database port, username, password, database name on server.
#
#      OPTIONS: see function ’usage’ below
# REQUIREMENTS: ---
#         BUGS: ---
#        NOTES: ---
#       AUTHOR: Martynas Stakė, martynas@idega.com
#      COMPANY: Idega, Vilnius
#      VERSION: 1.0
#      CREATED: 29.06.2011 - 17:00:00
#     REVISION: 29.06.2011
#===================================================================================

#Bedework server configuration
#This directory
DIRECTORY=$(cd `dirname $0` && pwd);
#server.xml settings file
SERVER_XML_FILENAME_BEGIN="jboss-5.1.0.GA/server/";
SERVER_CONFIGURATION_NAME="";
SERVER_XML_FILENAME_END="/deploy/jbossweb.sar/server.xml";
SERVER_FILE="";
#Port number, which is needed to use.
PORT_NUMBER="8080";
PORT_NUMBER_TO_WRITE="8085";
USER_FILE_FOR_PORT_CHANGE="cal.options.xml"

#Bedework database configuration files
PATH_TO_CONF_FOLDERS="bedework/config/bwbuild/";
USER_CONFIGURATION_FOLDER_NAME="User-";
DATABASE_CONFIGURATION_NAME="";
DATASOURCE_FILENAME="bedework-ds.xml";
DEFAULT_USERNAME="bedework";
DEFAULT_PASSWORD="bedework";
DEFAULT_DATABASE="bedework3p7";
DEFAULT_PORT="0";

textArray[0]="";
iterator=0;
# User define Function (UDF)
processLine(){
    local line="$@"; # get all args
    textArray[$iterator]=$line'\n'
    iterator=$(($iterator+1))
}

#=== FUNCTION ================================================================
#         NAME: create_database_configuration()
# DESCRIPTION: Copies or replaces 
#               skeleton of configuration files in bedework/config/bwbuild/
#===============================================================================
create_database_configuration(){
    local database_name="";
    local empty_string="";
    
    while [[ $database_name = $empty_string ]]; do
        echo "What database will you use?: Press enter for \"Derby\". \c ";
        echo "Derby"
        echo "H2"
        echo "MySQL"
        echo "PostgreSQL"
        echo "Liferay5"
        echo "Oracle10g"
    
        read database_name;
        if [ -z "$database_name" ]; then
            database_name="default";
        fi

        case $database_name in
            Derby)      database_name="default" ;;
            H2)         database_name="jboss-h2" ;;
            MySQL)      database_name="jboss-mysql" ;;
            PostgreSQL) database_name="jboss-postgresql" ;;
            Liferay5)   database_name="liferay5" ;;
            Oracle10g)  database_name="oracle10g" ;;
            default) ;;
            *)  echo "Wrong name, try again:-)";
            database_name="";
            ;;
        esac
    done
    DATABASE_CONFIGURATION_NAME=$database_name;

    if [ -d "$DIRECTORY/$USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME" ]; then
        rm -r -f $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME;
        rm -r -f ".platform"
        rm -r -f ".portal"
    fi
    
    cp -r -i $PATH_TO_CONF_FOLDERS$DATABASE_CONFIGURATION_NAME $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME;
    
    #Copy configurations
    cp -r -i $PATH_TO_CONF_FOLDERS"/.platform" ".platform";
    cp -r -i $PATH_TO_CONF_FOLDERS"/.portal" ".portal";
    
    echo "Settings created. Your configuration path is:" $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME;
}

#=== FUNCTION ================================================================
#         NAME: configure_derby
# DESCRIPTION: Edits User- bedework-ds.xml.
# PARAMETER 1: Database username
# PARAMETER 2: Database password
# PARAMETER 3: Database name
# PARAMETER 4: Database port
#===============================================================================
configure_derby(){
    sed -i s/sa/$1/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    sed -i s/bw/$2/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    
    if [[ $3 != $DEFAULT_DATABASE ]] ; then
        sed -i s/"\${jboss.server.data.dir}\/bedework\/derby\/CalDb3p6"/$3/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
    
    if [[ $4 != $DEFAULT_PORT ]] ; then
        sed -i s/1527/$4/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
}

#=== FUNCTION ================================================================
#         NAME: configure_h2
# DESCRIPTION: Edits User- bedework-ds.xml.
# PARAMETER 1: Database username
# PARAMETER 2: Database password
# PARAMETER 3: Database name
# PARAMETER 4: Database port
#===============================================================================
configure_h2(){
    sed -i s/sa/$1/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    sed -i s/bw/$2/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    
    if [[ $3 != $DEFAULT_DATABASE ]] ; then
        sed -i s/"\${jboss.server.data.dir}\/bedework\/h2\/CalDb3p6"/$3/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
    
    if [[ $4 != $DEFAULT_PORT ]] ; then
        sed -i s/9092/$4/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
}

#=== FUNCTION ================================================================
#         NAME: configure_mysql
# DESCRIPTION: Edits User- bedework-ds.xml.
# PARAMETER 1: Database username
# PARAMETER 2: Database password
# PARAMETER 3: Database name
# PARAMETER 4: Database port
#===============================================================================
configure_mysql(){
    sed -i s/"\[\[\[yourDbUserNameHere\]\]\]"/$1/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    sed -i s/"\[\[\[yourDbPasswordHere\]\]\]"/$2/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    
    if [[ $3 != $DEFAULT_DATABASE ]] ; then
        sed -i s/bedework3p7/$3/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
    
    if [[ $4 != $DEFAULT_PORT ]] ; then
        sed -i s/3306/$4/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
    
    cd $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME;
    cd "lib/server"
    wget http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.16.zip/from/ftp://ftp.fu-berlin.de/unix/databases/mysql/
    cd ..
    cd ..
    cd ..
}

#=== FUNCTION ================================================================
#         NAME: configure_postgresql
# DESCRIPTION: Edits User- bedework-ds.xml.
# PARAMETER 1: Database username
# PARAMETER 2: Database password
# PARAMETER 3: Database name
# PARAMETER 4: Database port
#===============================================================================
configure_postgresql(){
    sed -i s/bedework/$1/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    sed -i s/"<password><\/password>"/"<password>"$2"<\/password>"/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    
    if [[ $3 != $DEFAULT_DATABASE ]] ; then
        sed -i s/$1"3p7"/$3/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
    
    if [[ $4 != $DEFAULT_PORT ]] ; then
        sed -i s/5432/$4/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
}

#=== FUNCTION ================================================================
#         NAME: configure_liferay5
# DESCRIPTION: Edits User- bedework-ds.xml.
# PARAMETER 1: Database username
# PARAMETER 2: Database password
# PARAMETER 3: Database name
# PARAMETER 4: Database port
#===============================================================================
configure_liferay5(){
echo "Sorry, nothing to change:-D"
}

#=== FUNCTION ================================================================
#         NAME: configure_oracle10g
# DESCRIPTION: Edits User- bedework-ds.xml.
# PARAMETER 1: Database username
# PARAMETER 2: Database password
# PARAMETER 3: Database name
# PARAMETER 4: Database port
#===============================================================================
configure_oracle10g(){
    sed -i s/"\[\[\[yourDbUserNameHere\]\]\]"/$1/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    sed -i s/"\[\[\[yourDbPasswordHere\]\]\]"/$2/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    
    if [[ $4 != $DEFAULT_PORT ]] ; then
        sed -i s/1521/$4/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    fi
    
    echo "Please type Oracle database host. Press enter for \"localhost\":";
    read database_host;
    if [ -z "$database_host" ]; then
        database_host="127.0.0.1";
    fi
    sed -i s/youroraclehost/$database_host/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
    
    echo "Please type Oracle database sid. Press enter for 0:";
    read sid;
    if [ -z "$sid" ]; then
        sid="0";
    fi
    sed -i s/yoursid/$sid/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME'/'$DATASOURCE_FILENAME;
}

#=== FUNCTION ================================================================
#         NAME: edit_database_configuration()
# DESCRIPTION:  Let to enter existing database configuration usernamer, password,
#               database, port.
#===============================================================================
edit_database_configuration(){
    local username="";
    local password="";
    local database="";
    local port="";
    
    echo "Please type database username. Press enter for \"bedework\":";
    read username;
    if [ -z "$username" ]; then
        username="bedework";
    fi
    
    echo "Please type database password. Press enter for \"bedework\":";
    read password;
    if [ -z "$password" ]; then
        password="bedework";
    fi
    
    echo "Please type database name. Press enter for \"bedework3p7\":";
    read database;
    if [ -z "$database" ]; then
        database="bedework3p7";
    fi
    
    echo "Please type database port number. Press enter for default database number:";
    read port;
    if [ -z "$port" ]; then
        port="0";
    fi
    
    case $DATABASE_CONFIGURATION_NAME in
        default)            configure_derby $username $password $database $port ;;
        jboss-h2)           configure_h2 $username $password $database $port ;;
        jboss-mysql)        configure_mysql $username $password $database $port ;;
        jboss-postgresql)   configure_postgresql $username $password $database $port ;;
        liferay5)           configure_liferay5 $username $password $database $port ;;
        oracle10g)          configure_oracle10g $username $password $database $port ;;
        *)  echo "Database configuration was not found."
        exit 1;
        ;;
    esac
    
    echo "You successfully changed "$DATABASE_CONFIGURATION_NAME" configuration."
    sed -i s/$PORT_NUMBER/$PORT_NUMBER_TO_WRITE/g $USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME"/"$USER_FILE_FOR_PORT_CHANGE;
    echo "Please initiate database. You can do it at localhost:$PORT_NUMBER_TO_WRITE/jmx-console";
    echo "Username: admin."
    echo "Password can be found at $DIRECTORY/jboss-5.1.0.GA/server/default/conf/props/jmx-console-users.properties";
    echo "You must do the following steps:"
    echo "Select “org.bedework” from the “Object Name Filter” menu to the left of the page."
    echo "Select “service=DumpRestore”"
    echo "You will be presented with a form allowing you to manage the dump/restore process."
    echo "The DataIn attribute should point at the xml datafile you wish to restore. "
    echo "By default, this is the initbedework.xml file. "
    echo "Initialize the schema:"
    echo "Set the attribute Export to True"
    echo "Set the attribute Create to True"
    echo "Click Apply Changes to set the values."
    echo "Click Invoke for the schema operation."
    echo "Restore the data (Read the note below)"
    echo "Navigate back to the DumpRestore service (click Back to MBean)"
    echo "Click Invoke for the restoreData operation."
    echo "See https://wiki.jasig.org/display/BWK/BW+3.7+Introducing+Bedework for futher configuration."
}

#=== FUNCTION ================================================================
#         NAME: edit_server_configuration
# DESCRIPTION: Display usage information for this script.
#===============================================================================
edit_server_configuration(){
    echo "Please type the Bedework server configuration name. Press enter for \"default\":";
    read SERVER_CONFIGURATION_NAME;
    if [ -z "$SERVER_CONFIGURATION_NAME" ]; then
        SERVER_CONFIGURATION_NAME="default";
    fi
    echo Using $SERVER_CONFIGURATION_NAME configuration

    echo "Please type the Bedework jBoss server port. Press enter for \"8080:\"";
    read PORT_NUMBER_TO_WRITE;
    if [ ! `expr $PORT_NUMBER_TO_WRITE + 1 2> /dev/null` ] ; then
        echo "Not number!" $PORT_NUMBER_TO_WRITE "Need something like 8080";
        exit 1;
    fi
    if [ -z "$PORT_NUMBER_TO_WRITE" ] ; then
        PORT_NUMBER_TO_WRITE="8080";
    fi
    echo Using $PORT_NUMBER_TO_WRITE
    
    SERVER_FILE=$SERVER_XML_FILENAME_BEGIN$SERVER_CONFIGURATION_NAME$SERVER_XML_FILENAME_END;
    # make sure file exist and readable
    if [ ! -f $SERVER_FILE ] ; then
        echo "$FILE : does not exists"
        exit 1
    elif [ ! -r $SERVER_FILE ] ; then
        echo "$SERVER_FILE: can not read"
        exit 2
    fi
    
    # read $FILE using the file descriptors
    # Set loop separator to end of line
    BAKIFS=$IFS
    IFS=$(echo -en "\n\b")
    exec 3<&0
    exec 0<"$SERVER_FILE"
    while read -r line
    do
        # use $line variable to process line in processLine() function
        processLine $line
    done
    exec 0<&3
 
    # restore $IFS which was used to determine what the field separators are
    IFS=$BAKIFS

    #Initialize file by overwriting
    echo "" > $SERVER_FILE
    mark=1 #means false
    for i in ${textArray[@]}; do
        if [ $mark = 0 ]; then
            if [[ $i = *port=* ]] ; then
                    #PORT_NUMBER="${i//[!0-9]}";
                    echo -n -e "port=\""$PORT_NUMBER_TO_WRITE"\""' ' >> $SERVER_FILE;
            fi
            mark=1;
        else
            echo -n -e $i' ' >> $SERVER_FILE
        fi
    
        if [[ $i = *protocol=\"HTTP/1.1\"* ]] ; then
            mark=0; #means true
        fi
    done
}

### Main script stars here ###
echo "Hello, use numbers to define primary menu and words other menus.";

OPTIONS="Configure Start Quit"
BEDEWORK_CONFIGURATION=$USER_CONFIGURATION_FOLDER_NAME$DATABASE_CONFIGURATION_NAME;
select opt in $OPTIONS; do
    if [ "$opt" = "Quit" ]; then
        echo done
        exit
    elif [ "$opt" = "Configure" ]; then
        edit_server_configuration
        create_database_configuration;
        edit_database_configuration;
    elif [ "$opt" = "Start" ]; then
        echo "Please type the Bedework folder name of user configuration: ";
        read BEDEWORK_CONFIGURATION;
        if [ -z "$BEDEWORK_CONFIGURATION" ]; then
            echo "Name must be given. Try Again!"
            exit
        fi
        
        if [ ! -d "$DIRECTORY/$BEDEWORK_CONFIGURATION" ]; then
            echo "Given directory $DIRECTORY/$BEDEWORK_CONFIGURATION does not exist. Try again!"
            exit
        fi
        
        ./bw -bwchome $DIRECTORY -bwc $BEDEWORK_CONFIGURATION clean.deploy.debug;
        ./bw -bwchome $DIRECTORY -bwc $BEDEWORK_CONFIGURATION -tzsvr;
        ./bw -bwchome $DIRECTORY -bwc $BEDEWORK_CONFIGURATION -carddav;
        ./bw -bwchome $DIRECTORY -bwc $BEDEWORK_CONFIGURATION -carddav deploy-addrbook;
        xterm -e ./bw -quickstart dirstart & ./startjboss
    else
        clear
        echo bad option
    fi
done

exit 0;

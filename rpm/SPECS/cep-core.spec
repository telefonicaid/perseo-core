# Copyright 2015 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of perseo-core.
#
# perseo-core is free software: you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# perseo-core is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with perseo-core. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# iot_support at tid dot es

Summary: Perseo Complex Event Processing Core
Name: perseo-cep-core
Version: %{_product_version}
Release: %{_product_release}
License: AGPLv3
BuildRoot: %{_topdir}/BUILDROOT/
BuildArch: x86_64
Requires: tomcat, logrotate
Group: Applications/Engineering
Vendor: Telefonica I+D

%description
Perseo is the Complex Event Process for the Fiware platform.

# System folders
%define _install_dir /usr/share/tomcat/webapps
%define _perseoCepCore_log_dir /var/log/perseo

# RPM Building folder
%define _build_root_project %{buildroot}%{_install_dir}
# -------------------------------------------------------------------------------------------- #
# prep section, setup macro:
# -------------------------------------------------------------------------------------------- #
%prep
echo "[INFO] Preparing installation"
# Create rpm/BUILDROOT folder
rm -Rf $RPM_BUILD_ROOT && mkdir -p $RPM_BUILD_ROOT
[ -d %{_build_root_project} ] || mkdir -p %{_build_root_project}

#FIXME: this line should be adjusted by the release.sh (not yet in the repo). Once the release.sh is ok with this, please remove this fixme line
cp -ax %{_topdir}/../target/perseo-core-%{_product_version}.war %{_build_root_project}/perseo-core.war

cp -R %{_topdir}/SOURCES/etc %{buildroot}

# -------------------------------------------------------------------------------------------- #
# post-install section:
# -------------------------------------------------------------------------------------------- #
%post

echo "[INFO] Configuring application"

    echo "[INFO] Creating log directory"
    mkdir -p %{_perseoCepCore_log_dir}
    chown -R %{_project_user}:%{_project_user} %{_perseoCepCore_log_dir}
    chown -R %{_project_user}:%{_project_user} _install_dir
    chmod g+s %{_perseoCepCore_log_dir}
    setfacl -d -m g::rwx %{_perseoCepCore_log_dir}
    setfacl -d -m o::rx %{_perseoCepCore_log_dir}

echo "Done"

# -------------------------------------------------------------------------------------------- #
# pre-uninstall section:
# -------------------------------------------------------------------------------------------- #
%preun

if [ $1 == 0 ]; then

  echo "[INFO] Removing application log files"
  # Log
  [ -d %{_perseoCepCore_log_dir} ] && rm -rfv %{_perseoCepCore_log_dir}

  echo "[INFO] Removing application files"
  # Installed files
  rm -rfv %{_install_dir}/perseo-core*

fi

%postun
%clean
rm -rf $RPM_BUILD_ROOT

# -------------------------------------------------------------------------------------------- #
# Files to add to the RPM
# -------------------------------------------------------------------------------------------- #
%files
%defattr(755,%{_project_user},%{_project_user},755)
%config /etc/sysconfig/logrotate-perseo-core-size
%config /etc/logrotate.d/logrotate-perseo-core.conf
%config /etc/cron.d/cron-logrotate-perseo-core-size
%{_install_dir}/perseo-core.war

%changelog
* Fri Feb 08 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.3.0
- Add: support for esper 6.1.0 timed rules("timer:XX" patterns and Match Recognize interval patterns) (#91)
- Add: environment variable based configuration (PERSEO_FE_URL and MAX_AGE)
- Add: travis.yml file for Travis CI. All branches with Java 8, 9, 10 and 11
- Fix: print perseo-core app logs instead of catalina in docker
- Upgrade org.slf4j (api & log4j) from 1.6.1 to 1.7.25
- Upgrade junit from 4.10 to 4.12
- Upgrade jetty-servlet from 9.2.0.M1 to 9.4.12.v20180830
- Upgrade javaee-web-api from 6.0 to 8.0
- Upgrade commons-logging from 1.1.3 to 1.2
- Upgrade maven-compiler-plugin from 2.3.2 to 3.8.0
- Upgrade maven-compiler source and target from 1.6 to 1.8
- Upgrade maven-war-plugin from 2.1.1 to 3.2.2
- Upgrade maven-dependency-plugin from 2.1 to 3.1.1
- Upgrade maven-site-plugin from 3.3 to 3.7.1
- Upgrade cobertura-maven-plugin from 2.6 to 2.7
- Upgrade maven-surefire-report-plugin from 2.16 to 2.22.0
- Upgrade maven-checkstyle-plugin from 2.12 to 3.0.0
- Upgrade maven-javadoc-plugin from 2.9 to 3.0.1

* Fri Jun 15 2018 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.2.0
- Upgrade Esper library from 4.7.0 to 6.1.0
- Upgrade to CentOS 7, Java 1.8.0, Tomcat 8.5.27 and Maven 3.5.3 in Dockerfile

* Wed Sep 28 2016 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.1.0
- Fix: Dockerfile to build the right version corresponding to tag (#59)
- Add: Add 'from' field in logs (#69)
- Add: Add GET for log level (#68)

* Fri Jun 03 2016 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.0.0
- Add: Change log level at runtime with PUT (#43)
- Add: Add srv and subsrv in log traces (#44)
- Add: Use fiware-correlator for correlator (#45)

* Wed Dec 09 2015 Fermin Galan <fermin.galanmarquez@telefonica.com> 0.3.0
- Minor changes and fixes in RPM packaging

* Fri May 22 2015 Daniel Moran <daniel.moranjimenez@telefonica.com> 0.2.0
- Add /version.jsp path for checking version
- Decrease log level for rules
- Fix perseo-core.properties for reference missing from WAR file (#19)
- Fix Use service/subservice (#21)
- Fix Use UTF-8 in responses and default encoding for request (#3)

* Mon Jan 19 2015 Carlos Romero Brox <brox@tid.es> 0.1.1
- Fix: change log format. Add time level. Remove class and line number from message

* Fri Nov 7 2014 Carlos Romero Brox <brox@tid.es> 0.1.0
- Initial version

package fr.cls.atoll.motu.web.common.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import fr.cls.atoll.motu.api.message.xml.ErrorType;
import fr.cls.atoll.motu.web.bll.exception.MotuException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFUtils {

    public static void changeDimensionAndVariableName(Path netCDFDirectoryPath, String originalFileName, String newFileName, Path outputFilePath)
            throws MotuException {
        try {
            String ncMLDataFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            ncMLDataFile += "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\""
                    + Paths.get(netCDFDirectoryPath.toString(), newFileName).toString() + "\">";

            NetcdfFile ncFileOriginel = NetcdfFile.open(Paths.get(netCDFDirectoryPath.toString(), originalFileName).toString());
            NetcdfFile ncFileNewFile = NetcdfFile.open(Paths.get(netCDFDirectoryPath.toString(), newFileName).toString());

            List<Variable> originelVariableList = ncFileOriginel.getVariables();
            List<Variable> resultVariableList = ncFileNewFile.getVariables();
            List<String> alreadyTreatedDimension = new ArrayList<>();

            for (Variable resultVariable : resultVariableList) {
                for (Variable originelVariable : originelVariableList) {
                    if (resultVariable.findAttribute("standard_name").getStringValue()
                            .equals(originelVariable.findAttribute("standard_name").getStringValue())) {
                        if (!resultVariable.getShortName().equals(originelVariable.getShortName())) {
                            ncMLDataFile += "<variable name=\"" + originelVariable.getShortName() + "\" orgName=\"" + resultVariable.getShortName()
                                    + "\"" + "/>";

                        }
                        List<Dimension> originalDims = originelVariable.getDimensions();
                        List<Dimension> newFileDims = resultVariable.getDimensions();
                        if (originalDims.size() == 1 && newFileDims.size() == 1) {
                            Dimension originalDim = originalDims.get(0);
                            Dimension newFileDim = newFileDims.get(0);
                            if (!alreadyTreatedDimension.contains(originalDim.getName()) && !originalDim.getName().equals(newFileDim.getName())) {
                                ncMLDataFile += "<dimension name=\"" + originalDim.getName() + "\" orgName=\"" + newFileDim.getName() + "\" length=\""
                                        + originalDim.getLength() + "\"/>";
                                alreadyTreatedDimension.add(originalDim.getName());
                            }
                        }
                    }
                }
            }
            ncMLDataFile += "</netcdf>";

            FileWriter fw = new FileWriter(Paths.get(netCDFDirectoryPath.toString(), "rename.xml").toFile());
            fw.write(ncMLDataFile);
            fw.close();

            String[] mainParams = { "-in", Paths.get(netCDFDirectoryPath.toString(), "rename.xml").toString(), "-out", outputFilePath.toString() };
            ucar.nc2.dataset.NetcdfDataset.main(mainParams);
        } catch (IOException e) {
            throw new MotuException(ErrorType.SYSTEM, e);
        }

    }
}
